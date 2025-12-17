-- Seed básico de datos para iw3_2025_prod (MySQL)
-- Refleja exactamente lo que se envía por Postman (sin IDs, JPA los genera)
-- Ejecutar UNA SOLA VEZ luego de que el backend haya creado el esquema vacío (ddl-auto=update)

-- Productos (JPA genera UUID automáticamente)
INSERT INTO products (product_name, description)
VALUES
  ('GLP','Gas Licuado de Petróleo'),
  ('GasOil','Diésel grado 2'),
  ('Nafta Super','Gasolina 95 octanos');

-- Clientes (JPA genera UUID automáticamente)
INSERT INTO customers (social_number, phone_number, mail)
VALUES
  (30123456789, 1122334455, 'cliente1@empresa.com'),
  (30234567890, 1133445566, 'cliente2@empresa.com'),
  (30345678901, 1144556677, 'cliente3@empresa.com');

-- Conductores (JPA genera UUID automáticamente)
INSERT INTO drivers (name, last_name, dni)
VALUES
  ('Juan','Pérez',12345678),
  ('María','Gómez',23456789),
  ('Luis','Rodríguez',34567890);

-- Camiones CON cisternas (JPA genera UUID para Truck, IDENTITY para Cistern)
-- Truck 1: ABC123 con 2 cisternas
INSERT INTO trucks (license_plate, description)
VALUES ('ABC123','Camión cisterna grande');

SET @truck1_id = (SELECT id FROM trucks WHERE license_plate='ABC123');
INSERT INTO cisterns (capacity, licence_plate, truck_id)
VALUES 
  (3000.0, 'C1-ABC', @truck1_id),
  (3000.0, 'C2-ABC', @truck1_id);

-- Truck 2: DEF456 con 3 cisternas
INSERT INTO trucks (license_plate, description)
VALUES ('DEF456','Camión cisterna mediano');

SET @truck2_id = (SELECT id FROM trucks WHERE license_plate='DEF456');
INSERT INTO cisterns (capacity, licence_plate, truck_id)
VALUES 
  (2000.0, 'C1-DEF', @truck2_id),
  (2000.0, 'C2-DEF', @truck2_id),
  (1500.0, 'C3-DEF', @truck2_id);

-- Truck 3: GHI789 con 1 cisterna
INSERT INTO trucks (license_plate, description)
VALUES ('GHI789','Camión cisterna compacto');

SET @truck3_id = (SELECT id FROM trucks WHERE license_plate='GHI789');
INSERT INTO cisterns (capacity, licence_plate, truck_id)
VALUES 
  (5000.0, 'C1-GHI', @truck3_id);


