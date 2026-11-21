USE iron_legacy;

-- Planes. Precios provisionales: la tarifa real la entrega el administrador.
INSERT INTO plan_membresia (nombre, duracion_meses, precio) VALUES
  ('Mensual',    1, 1.00),  -- TARIFA PENDIENTE
  ('Trimestral', 3, 1.00);  -- TARIFA PENDIENTE

-- Usuarios de demostración. Solo se guarda el hash BCrypt; las claves están en el README.
INSERT INTO usuario (nombre_completo, nombre_usuario, clave_hash, rol) VALUES
  ('Administrador Demo', 'admin',     '$2a$10$mPcX716Tc9UQ0MRGgTp8HeiodwndQ5DQY7sQMANDYKeJtkGgeSB8O', 'ADMINISTRADOR'),
  ('Recepcionista Demo', 'recepcion', '$2a$10$hQ5R7NWIFgExryMF.ddSgOxQl8ffwfqDVh2l89ZLZc1TOUEtmQJHG', 'RECEPCIONISTA');

-- Los socios de demostración los crea config/DatosDemo (perfil demo) con fechas relativas a hoy.
