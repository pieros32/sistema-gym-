CREATE DATABASE IF NOT EXISTS iron_legacy CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE iron_legacy;

CREATE TABLE usuario (
  id_usuario      INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre_completo VARCHAR(100) NOT NULL,
  nombre_usuario  VARCHAR(30)  NOT NULL,
  clave_hash      VARCHAR(255) NOT NULL,
  rol             ENUM('RECEPCIONISTA','ADMINISTRADOR') NOT NULL,
  activo          TINYINT(1)   NOT NULL DEFAULT 1,
  CONSTRAINT uq_usuario_nombre UNIQUE (nombre_usuario)
) ENGINE=InnoDB;

CREATE TABLE socio (
  id_socio        INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre_completo VARCHAR(100) NOT NULL,
  dni             CHAR(8)      NOT NULL,
  telefono        CHAR(9)      NOT NULL,
  fecha_registro  DATE         NOT NULL DEFAULT (CURRENT_DATE),
  CONSTRAINT uq_socio_dni UNIQUE (dni),
  CONSTRAINT ck_socio_dni CHECK (dni REGEXP '^[0-9]{8}$'),
  CONSTRAINT ck_socio_telefono CHECK (telefono REGEXP '^[0-9]{9}$')
) ENGINE=InnoDB;

CREATE TABLE plan_membresia (
  id_plan        INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre         VARCHAR(30)  NOT NULL,
  duracion_meses TINYINT UNSIGNED NOT NULL,
  precio         DECIMAL(8,2) NOT NULL,
  CONSTRAINT uq_plan_nombre UNIQUE (nombre),
  CONSTRAINT ck_plan_duracion CHECK (duracion_meses > 0),
  CONSTRAINT ck_plan_precio CHECK (precio >= 0)
) ENGINE=InnoDB;

CREATE TABLE membresia (
  id_membresia      INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_socio          INT UNSIGNED NOT NULL,
  id_plan           INT UNSIGNED NOT NULL,
  fecha_inicio      DATE NOT NULL,
  fecha_vencimiento DATE NOT NULL,
  CONSTRAINT fk_membresia_socio FOREIGN KEY (id_socio) REFERENCES socio (id_socio),
  CONSTRAINT fk_membresia_plan  FOREIGN KEY (id_plan)  REFERENCES plan_membresia (id_plan),
  CONSTRAINT ck_membresia_fechas CHECK (fecha_vencimiento > fecha_inicio),
  INDEX idx_membresia_socio_venc (id_socio, fecha_vencimiento),
  INDEX idx_membresia_venc (fecha_vencimiento)
) ENGINE=InnoDB;

CREATE TABLE pago (
  id_pago      INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_membresia INT UNSIGNED NOT NULL,
  id_usuario   INT UNSIGNED NOT NULL,
  monto        DECIMAL(8,2) NOT NULL,
  fecha_pago   DATE NOT NULL,
  medio_pago   ENUM('EFECTIVO','YAPE','PLIN') NOT NULL,
  CONSTRAINT uq_pago_membresia UNIQUE (id_membresia),
  CONSTRAINT fk_pago_membresia FOREIGN KEY (id_membresia) REFERENCES membresia (id_membresia),
  CONSTRAINT fk_pago_usuario   FOREIGN KEY (id_usuario)   REFERENCES usuario (id_usuario),
  CONSTRAINT ck_pago_monto CHECK (monto > 0),
  INDEX idx_pago_fecha (fecha_pago)
) ENGINE=InnoDB;

CREATE TABLE asistencia (
  id_asistencia     INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_socio          INT UNSIGNED NOT NULL,
  id_usuario        INT UNSIGNED NOT NULL,
  fecha_hora        DATETIME NOT NULL,
  resultado         ENUM('AUTORIZADO','DENEGADO') NOT NULL,
  motivo_denegacion VARCHAR(60) NULL,
  CONSTRAINT fk_asistencia_socio   FOREIGN KEY (id_socio)   REFERENCES socio (id_socio),
  CONSTRAINT fk_asistencia_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
  INDEX idx_asistencia_socio_fecha (id_socio, fecha_hora)
) ENGINE=InnoDB;
