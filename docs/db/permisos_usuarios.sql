-- ============================================================
-- CONFIGURACIÓN DE PERMISOS PARA USUARIOS NO ADMINISTRADORES
-- Base de datos: iw3_2025_prod (MySQL)
-- ============================================================
-- Este script configura permisos de lectura (SELECT) para usuarios 
-- VIEWER y OPERADOR que acceden a través de la aplicación.
-- Los usuarios de la aplicación NO necesitan usuarios MySQL directos,
-- ya que la app se conecta con un usuario único (user@localhost).
-- Este script es solo para consultas directas desde Adminer/consola.
-- ============================================================

USE iw3_2025_prod;

-- ============================================================
-- OPCIÓN 1: Crear usuarios MySQL para acceso directo a BD
-- (solo si necesitas que accedan vía Adminer/MySQL Workbench)
-- ============================================================

-- Usuario de solo lectura (VIEWER)
DROP USER IF EXISTS 'viewer_user'@'%';
CREATE USER 'viewer_user'@'%' IDENTIFIED BY 'viewer_password_2025';
GRANT SELECT ON iw3_2025_prod.* TO 'viewer_user'@'%';

-- Usuario con permisos de lectura + escritura limitada (OPERADOR)
DROP USER IF EXISTS 'operador_user'@'%';
CREATE USER 'operador_user'@'%' IDENTIFIED BY 'operador_password_2025';
GRANT SELECT ON iw3_2025_prod.* TO 'operador_user'@'%';
GRANT INSERT, UPDATE ON iw3_2025_prod.order_details TO 'operador_user'@'%';
GRANT UPDATE(state) ON iw3_2025_prod.orders TO 'operador_user'@'%';

-- Aplicar cambios
FLUSH PRIVILEGES;


-- ============================================================
-- OPCIÓN 2: Crear VISTAS para usuarios no administradores
-- (recomendado para limitar qué columnas pueden ver)
-- ============================================================

-- Vista de órdenes sin datos sensibles (sin password, sin activation_password)
CREATE OR REPLACE VIEW view_orders_public AS
SELECT 
    id,
    external_code,
    preset,
    state,
    time_initial_reception,
    time_initial_weighing,
    time_final_weighing,
    initial_weighing,
    final_weighing,
    neto,
    customer_id,
    driver_id,
    product_id,
    truck_id
FROM orders;

-- Vista de usuarios sin contraseñas
CREATE OR REPLACE VIEW view_users_public AS
SELECT 
    id,
    username,
    role,
    enabled,
    created_at
FROM users;

-- Vista de detalles de órdenes con información agregada
CREATE OR REPLACE VIEW view_order_details_full AS
SELECT 
    od.id,
    od.temperature,
    od.flow,
    od.density,
    od.mass_accumulated,
    od.created_at,
    od.order_id,
    o.external_code,
    o.state AS order_state,
    p.product_name,
    c.mail AS customer_mail,
    d.name AS driver_name,
    d.last_name AS driver_last_name,
    t.license_plate AS truck_license
FROM order_details od
INNER JOIN orders o ON od.order_id = o.id
LEFT JOIN products p ON o.product_id = p.id
LEFT JOIN customers c ON o.customer_id = c.id
LEFT JOIN drivers d ON o.driver_id = d.id
LEFT JOIN trucks t ON o.truck_id = t.id;

-- Vista de conciliación (estado 4 solamente)
CREATE OR REPLACE VIEW view_reconciliation AS
SELECT 
    o.id AS order_id,
    o.external_code,
    o.initial_weighing,
    o.final_weighing,
    o.neto,
    o.preset,
    (o.final_weighing - o.initial_weighing) AS neto_balanza,
    (SELECT MAX(mass_accumulated) FROM order_details WHERE order_id = o.id) AS mass_flowmeter,
    ((o.final_weighing - o.initial_weighing) - (SELECT COALESCE(MAX(mass_accumulated), 0) FROM order_details WHERE order_id = o.id)) AS diferencia,
    (SELECT AVG(temperature) FROM order_details WHERE order_id = o.id) AS avg_temperature,
    (SELECT AVG(density) FROM order_details WHERE order_id = o.id) AS avg_density,
    (SELECT AVG(flow) FROM order_details WHERE order_id = o.id) AS avg_flow,
    p.product_name,
    c.mail AS customer_mail,
    d.name AS driver_name,
    t.license_plate AS truck_license,
    o.time_final_weighing
FROM orders o
LEFT JOIN products p ON o.product_id = p.id
LEFT JOIN customers c ON o.customer_id = c.id
LEFT JOIN drivers d ON o.driver_id = d.id
LEFT JOIN trucks t ON o.truck_id = t.id
WHERE o.state = 4;

-- Vista de log de cambios de estado
CREATE OR REPLACE VIEW view_order_state_logs_full AS
SELECT 
    osl.id,
    osl.actor,
    osl.changed_at,
    osl.from_state,
    osl.to_state,
    osl.notes,
    osl.order_id,
    o.external_code,
    CASE osl.from_state
        WHEN 0 THEN 'Creado'
        WHEN 1 THEN 'Pendiente pesaje inicial'
        WHEN 2 THEN 'Carga en progreso'
        WHEN 3 THEN 'Pendiente pesaje final'
        WHEN 4 THEN 'Finalizado'
        ELSE 'Desconocido'
    END AS from_state_description,
    CASE osl.to_state
        WHEN 0 THEN 'Creado'
        WHEN 1 THEN 'Pendiente pesaje inicial'
        WHEN 2 THEN 'Carga en progreso'
        WHEN 3 THEN 'Pendiente pesaje final'
        WHEN 4 THEN 'Finalizado'
        ELSE 'Desconocido'
    END AS to_state_description
FROM order_state_logs osl
INNER JOIN orders o ON osl.order_id = o.id;

-- Vista de alarmas de temperatura
CREATE OR REPLACE VIEW view_temperature_alarms_full AS
SELECT 
    ta.id,
    ta.temperature,
    ta.threshold,
    ta.state,
    ta.created_at,
    ta.ack_at,
    ta.ack_user,
    ta.ack_note,
    ta.order_id,
    o.external_code,
    o.state AS order_state,
    p.product_name,
    d.name AS driver_name,
    t.license_plate AS truck_license
FROM temperature_alarms ta
INNER JOIN orders o ON ta.order_id = o.id
LEFT JOIN products p ON o.product_id = p.id
LEFT JOIN drivers d ON o.driver_id = d.id
LEFT JOIN trucks t ON o.truck_id = t.id;


-- ============================================================
-- OPCIÓN 3: Otorgar permisos de SELECT a vistas (recomendado)
-- ============================================================

-- Si creaste usuarios específicos (OPCIÓN 1), otorga permisos a las vistas:
GRANT SELECT ON iw3_2025_prod.view_orders_public TO 'viewer_user'@'%';
GRANT SELECT ON iw3_2025_prod.view_users_public TO 'viewer_user'@'%';
GRANT SELECT ON iw3_2025_prod.view_order_details_full TO 'viewer_user'@'%';
GRANT SELECT ON iw3_2025_prod.view_reconciliation TO 'viewer_user'@'%';
GRANT SELECT ON iw3_2025_prod.view_order_state_logs_full TO 'viewer_user'@'%';
GRANT SELECT ON iw3_2025_prod.view_temperature_alarms_full TO 'viewer_user'@'%';

GRANT SELECT ON iw3_2025_prod.view_orders_public TO 'operador_user'@'%';
GRANT SELECT ON iw3_2025_prod.view_users_public TO 'operador_user'@'%';
GRANT SELECT ON iw3_2025_prod.view_order_details_full TO 'operador_user'@'%';
GRANT SELECT ON iw3_2025_prod.view_reconciliation TO 'operador_user'@'%';
GRANT SELECT ON iw3_2025_prod.view_order_state_logs_full TO 'operador_user'@'%';
GRANT SELECT ON iw3_2025_prod.view_temperature_alarms_full TO 'operador_user'@'%';

-- También permite ver tablas de maestros (productos, clientes, etc.)
GRANT SELECT ON iw3_2025_prod.products TO 'viewer_user'@'%', 'operador_user'@'%';
GRANT SELECT ON iw3_2025_prod.customers TO 'viewer_user'@'%', 'operador_user'@'%';
GRANT SELECT ON iw3_2025_prod.drivers TO 'viewer_user'@'%', 'operador_user'@'%';
GRANT SELECT ON iw3_2025_prod.trucks TO 'viewer_user'@'%', 'operador_user'@'%';
GRANT SELECT ON iw3_2025_prod.cisterns TO 'viewer_user'@'%', 'operador_user'@'%';

FLUSH PRIVILEGES;


-- ============================================================
-- CONSULTAS DE PRUEBA PARA VISTAS
-- ============================================================

-- Ver todas las vistas creadas
SELECT TABLE_NAME, TABLE_TYPE 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'iw3_2025_prod' 
  AND TABLE_TYPE = 'VIEW';

-- Probar vista de órdenes públicas
-- SELECT * FROM view_orders_public LIMIT 5;

-- Probar vista de usuarios públicos
-- SELECT * FROM view_users_public;

-- Probar vista de detalles de órdenes
-- SELECT * FROM view_order_details_full LIMIT 10;

-- Probar vista de conciliación (solo órdenes finalizadas)
-- SELECT * FROM view_reconciliation;

-- Probar vista de logs de estado
-- SELECT * FROM view_order_state_logs_full ORDER BY changed_at DESC LIMIT 10;

-- Probar vista de alarmas
-- SELECT * FROM view_temperature_alarms_full WHERE state = 'PENDING';


-- ============================================================
-- NOTAS IMPORTANTES
-- ============================================================
-- 1. Las vistas NO requieren cambios en el código Java/Spring Boot
-- 2. Los usuarios de la aplicación (admin123, viewer, operator, etc.) 
--    se autentican a nivel de aplicación (JWT), NO a nivel MySQL
-- 3. Las vistas son útiles para:
--    - Consultas desde Adminer sin exponer datos sensibles
--    - Reportes desde herramientas externas (Power BI, Tableau)
--    - Auditoría y análisis de datos
-- 4. Para eliminar una vista: DROP VIEW IF EXISTS nombre_vista;
-- 5. Las vistas se actualizan automáticamente cuando cambian las tablas
-- ============================================================
