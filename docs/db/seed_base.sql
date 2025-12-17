-- Seed básico de datos para iw3_2025_prod (MySQL)
-- Ajustado a entidades JPA (tablas con mayúsculas y UUID en id)
-- Ejecutar UNA SOLA VEZ luego de que el backend haya creado el esquema vacío (ddl-auto=update)

-- Productos (tabla: Products, columnas: id, productName, description)
INSERT INTO Products (id, productName, description)
VALUES
  ('00000000-0000-0000-0000-000000000001','GLP','Gas Licuado de Petróleo'),
  ('00000000-0000-0000-0000-000000000002','GasOil','Diésel grado 2'),
  ('00000000-0000-0000-0000-000000000003','Nafta Super','Gasolina 95 octanos');

-- Clientes (tabla: Customers, columnas: id, socialNumber, phoneNumber, mail)
INSERT INTO Customers (id, socialNumber, phoneNumber, mail)
VALUES
  ('00000000-0000-0000-0000-100000000001', 30123456789, 1122334455, 'cliente1@empresa.com'),
  ('00000000-0000-0000-0000-100000000002', 30234567890, 1133445566, 'cliente2@empresa.com'),
  ('00000000-0000-0000-0000-100000000003', 30345678901, 1144556677, 'cliente3@empresa.com');

-- Conductores (tabla: Drivers, columnas: id, name, lastName, dni)
INSERT INTO Drivers (id, name, lastName, dni)
VALUES
  ('00000000-0000-0000-0000-200000000001','Juan','Pérez',12345678),
  ('00000000-0000-0000-0000-200000000002','María','Gómez',23456789),
  ('00000000-0000-0000-0000-200000000003','Luis','Rodríguez',34567890);

-- Camiones (tabla: Trucks, columnas: id, licensePlate, description)
INSERT INTO Trucks (id, licensePlate, description)
VALUES
  ('00000000-0000-0000-0000-300000000001','ABC123','Camión cisterna grande'),
  ('00000000-0000-0000-0000-300000000002','DEF456','Camión cisterna mediano'),
  ('00000000-0000-0000-0000-300000000003','GHI789','Camión cisterna compacto');

-- Cisternas (tabla: Cisterns, columnas: id (IDENTITY), capacity, licence_plate, truck_id)
SET @truck1_id = (SELECT id FROM Trucks WHERE licensePlate='ABC123');
INSERT INTO Cisterns (capacity, licence_plate, truck_id)
VALUES 
  (3000.0, 'C1-ABC', @truck1_id),
  (3000.0, 'C2-ABC', @truck1_id);

SET @truck2_id = (SELECT id FROM Trucks WHERE licensePlate='DEF456');
INSERT INTO Cisterns (capacity, licence_plate, truck_id)
VALUES 
  (2000.0, 'C1-DEF', @truck2_id),
  (2000.0, 'C2-DEF', @truck2_id),
  (1500.0, 'C3-DEF', @truck2_id);

SET @truck3_id = (SELECT id FROM Trucks WHERE licensePlate='GHI789');
INSERT INTO Cisterns (capacity, licence_plate, truck_id)
VALUES 
  (5000.0, 'C1-GHI', @truck3_id);


