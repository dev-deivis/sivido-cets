-- SIVIDO - Script MySQL generado desde el diagrama 'ER SIVIDO' (Visual Paradigm: SIVIDO_CETS_U4 (2).vpp)
-- Requiere MySQL 8.0.16+ (para que se apliquen los CHECK).

DROP DATABASE IF EXISTS sivido;
CREATE DATABASE sivido CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE sivido;

CREATE TABLE `CriterioExclusion` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `criterio` VARCHAR(200) NOT NULL,
  `tipoRespuesta` VARCHAR(20) NOT NULL DEFAULT 'SiNo' COMMENT 'HU-4.1: forma de responder el criterio.',
  `activo` BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'HU-4.3: un criterio en uso se desactiva en lugar de borrarse.',
  `tipoExclusion` VARCHAR(20) NOT NULL,
  `duracionDias` INT COMMENT 'NULL cuando tipoExclusion = ''Permanente''.',
  PRIMARY KEY (`id`),
  CONSTRAINT `chk_CriterioExclusion_tipoRespuesta` CHECK (tipoRespuesta IN ('SiNo','SeleccionSimple')),
  CONSTRAINT `chk_CriterioExclusion_tipoExclusion` CHECK (tipoExclusion IN ('Temporal','Permanente')),
  CONSTRAINT `chk_CriterioExclusion_duracionDias` CHECK (duracionDias >= 0)
) ENGINE=InnoDB;

CREATE TABLE `Usuario` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `nombreCompleto` VARCHAR(120) NOT NULL,
  `telefono` VARCHAR(15) NOT NULL,
  `correo` VARCHAR(120) NOT NULL UNIQUE,
  `passwordHash` VARCHAR(255) NOT NULL,
  `fechaRegistro` DATE,
  `consentimientoContacto` BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'HU-1.4: el donador acepta que se revele su contacto al confirmar interés.',
  `exclusionPermanente` BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Se fija cuando un padecimiento excluye de forma permanente (antes en RegistroDonacion).',
  `consentimientoInteroperabilidad` BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'HU-7.2: el donador autoriza el intercambio de su información con instituciones externas.',
  `fechaConsentimiento` DATETIME COMMENT 'Momento en que se otorgó el consentimiento de interoperabilidad.',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

CREATE TABLE `PersonalCETS` (
  `Usuarioid` INT NOT NULL,
  `rol` VARCHAR(20) NOT NULL,
  `correoInstitucional` VARCHAR(120) NOT NULL UNIQUE COMMENT 'HU-6.1: cuenta institucional, distinta del correo de Usuario.',
  `activo` BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (`Usuarioid`),
  CONSTRAINT `chk_PersonalCETS_rol` CHECK (rol IN ('Personal','Administrador')),
  CONSTRAINT `fk_PersonalCETS_Usuarioid` FOREIGN KEY (`Usuarioid`) REFERENCES `Usuario` (`id`)
) ENGINE=InnoDB;

CREATE TABLE `Hospital` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(150) NOT NULL UNIQUE COMMENT 'Catálogo para autocompletar (HU-2.1).',
  `latitud` DECIMAL(9,6) NOT NULL,
  `longitud` DECIMAL(9,6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

CREATE TABLE `SolicitudReposicion` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `folioCETS` VARCHAR(30) NOT NULL COMMENT 'Único entre las solicitudes con estatus ''Activa'': lo valida la aplicación (HU-2.4); sin índice.',
  `tipoSanguineo` VARCHAR(2) NOT NULL,
  `factorRh` VARCHAR(1) NOT NULL,
  `descripcion` VARCHAR(250),
  `estatus` VARCHAR(20) NOT NULL DEFAULT 'Activa',
  `fechaHoraRegistro` DATETIME NOT NULL,
  `uuidPublico` VARCHAR(36) NOT NULL UNIQUE COMMENT 'UUID no predecible, generado al crear la solicitud (HU-2.1); la URL pública y el QR se generan con él y no se guardan.',
  `Usuarioid` INT NOT NULL COMMENT 'Creador de la solicitud; inmutable: lo garantiza la aplicación.',
  `PersonalCETSUsuarioid` INT COMMENT 'Personal del CETS que la atendió (HU-6.4); NULL mientras nadie la atiende.',
  `Hospitalid` INT NOT NULL COMMENT 'Hospital del catálogo (HU-2.1).',
  PRIMARY KEY (`id`),
  CONSTRAINT `chk_SolicitudReposicion_tipoSanguineo` CHECK (tipoSanguineo IN ('A','B','AB','O')),
  CONSTRAINT `chk_SolicitudReposicion_factorRh` CHECK (factorRh IN ('+','-')),
  CONSTRAINT `chk_SolicitudReposicion_estatus` CHECK (estatus IN ('Activa','Atendida','Cerrada','Cancelada')),
  CONSTRAINT `fk_SolicitudReposicion_Usuarioid` FOREIGN KEY (`Usuarioid`) REFERENCES `Usuario` (`id`),
  CONSTRAINT `fk_SolicitudReposicion_PersonalCETSUsuarioid` FOREIGN KEY (`PersonalCETSUsuarioid`) REFERENCES `PersonalCETS` (`Usuarioid`),
  CONSTRAINT `fk_SolicitudReposicion_Hospitalid` FOREIGN KEY (`Hospitalid`) REFERENCES `Hospital` (`id`)
) ENGINE=InnoDB;

CREATE TABLE `CuestionarioRespuesta` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `resultado` VARCHAR(10) NOT NULL,
  `fechaRespuesta` DATETIME NOT NULL,
  `tiempoEsperaDias` INT,
  `Usuarioid` INT NOT NULL,
  `SolicitudReposicionid` INT,
  PRIMARY KEY (`id`),
  CONSTRAINT `chk_CuestionarioRespuesta_resultado` CHECK (resultado IN ('Apto','NoApto')),
  CONSTRAINT `fk_CuestionarioRespuesta_Usuarioid` FOREIGN KEY (`Usuarioid`) REFERENCES `Usuario` (`id`),
  CONSTRAINT `fk_CuestionarioRespuesta_SolicitudReposicionid` FOREIGN KEY (`SolicitudReposicionid`) REFERENCES `SolicitudReposicion` (`id`)
) ENGINE=InnoDB;

CREATE TABLE `HistorialClinico` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `diagnostico` VARCHAR(200),
  `fechaDeteccion` DATE,
  `observaciones` VARCHAR(500),
  `Usuarioid` INT NOT NULL,
  `PersonalCETSUsuarioid` INT NOT NULL,
  `CriterioExclusionid` INT,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_HistorialClinico_Usuarioid` FOREIGN KEY (`Usuarioid`) REFERENCES `Usuario` (`id`),
  CONSTRAINT `fk_HistorialClinico_PersonalCETSUsuarioid` FOREIGN KEY (`PersonalCETSUsuarioid`) REFERENCES `PersonalCETS` (`Usuarioid`),
  CONSTRAINT `fk_HistorialClinico_CriterioExclusionid` FOREIGN KEY (`CriterioExclusionid`) REFERENCES `CriterioExclusion` (`id`)
) ENGINE=InnoDB;

CREATE TABLE `HistorialEstatusSolicitud` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `SolicitudReposicionid` INT NOT NULL COMMENT 'Solicitud cuyo estatus cambió (HU-6.4).',
  `PersonalCETSUsuarioid` INT NOT NULL COMMENT 'Personal del CETS que hizo el cambio (HU-6.4).',
  `estatusAnterior` VARCHAR(20) NOT NULL,
  `estatusNuevo` VARCHAR(20) NOT NULL,
  `fechaCambio` DATETIME NOT NULL COMMENT 'HU-6.4: momento del cambio de estatus.',
  PRIMARY KEY (`id`),
  CONSTRAINT `chk_HistorialEstatusSolicitud_estatusAnterior` CHECK (estatusAnterior IN ('Activa','Atendida','Cerrada','Cancelada')),
  CONSTRAINT `chk_HistorialEstatusSolicitud_estatusNuevo` CHECK (estatusNuevo IN ('Activa','Atendida','Cerrada','Cancelada')),
  CONSTRAINT `fk_HistorialEstatusSolicitud_SolicitudReposicionid` FOREIGN KEY (`SolicitudReposicionid`) REFERENCES `SolicitudReposicion` (`id`),
  CONSTRAINT `fk_HistorialEstatusSolicitud_PersonalCETSUsuarioid` FOREIGN KEY (`PersonalCETSUsuarioid`) REFERENCES `PersonalCETS` (`Usuarioid`)
) ENGINE=InnoDB;

CREATE TABLE `InstitucionExterna` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `nombreInstitucion` VARCHAR(150),
  `tipoConexion` VARCHAR(50),
  `contactoTecnico` VARCHAR(120),
  `activa` BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

CREATE TABLE `HistorialExterno` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `Usuarioid` INT NOT NULL COMMENT 'HU-7.3: historial externo, separado de RegistroDonacion.',
  `InstitucionExternaid` INT NOT NULL,
  `fechaDonacion` DATE NOT NULL,
  `observaciones` VARCHAR(500),
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_HistorialExterno_Usuarioid` FOREIGN KEY (`Usuarioid`) REFERENCES `Usuario` (`id`),
  CONSTRAINT `fk_HistorialExterno_InstitucionExternaid` FOREIGN KEY (`InstitucionExternaid`) REFERENCES `InstitucionExterna` (`id`)
) ENGINE=InnoDB;

CREATE TABLE `IntercambioExterno` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `InstitucionExternaid` INT NOT NULL COMMENT 'HU-7.2: log de intercambio con la institución externa.',
  `Usuarioid` INT NOT NULL,
  `fechaIntercambio` DATETIME NOT NULL,
  `direccion` VARCHAR(10) NOT NULL,
  `estatus` VARCHAR(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `chk_IntercambioExterno_direccion` CHECK (direccion IN ('Enviado','Recibido')),
  CONSTRAINT `fk_IntercambioExterno_InstitucionExternaid` FOREIGN KEY (`InstitucionExternaid`) REFERENCES `InstitucionExterna` (`id`),
  CONSTRAINT `fk_IntercambioExterno_Usuarioid` FOREIGN KEY (`Usuarioid`) REFERENCES `Usuario` (`id`)
) ENGINE=InnoDB;

CREATE TABLE `Interes` (
  `Usuarioid` INT NOT NULL,
  `SolicitudReposicionid` INT NOT NULL,
  `fechaInteres` DATETIME NOT NULL COMMENT 'HU-3.3: momento en que el donador confirmó interés.',
  PRIMARY KEY (`Usuarioid`, `SolicitudReposicionid`),
  CONSTRAINT `fk_Interes_Usuarioid` FOREIGN KEY (`Usuarioid`) REFERENCES `Usuario` (`id`),
  CONSTRAINT `fk_Interes_SolicitudReposicionid` FOREIGN KEY (`SolicitudReposicionid`) REFERENCES `SolicitudReposicion` (`id`)
) ENGINE=InnoDB;

CREATE TABLE `Notificacion` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `distanciaAproximada` DECIMAL(6,2) COMMENT 'Distancia aproximada en km (HU-3.2); nunca la ubicación exacta.',
  `estatus` VARCHAR(20),
  `fechaEnvio` DATETIME NOT NULL,
  `Usuarioid` INT NOT NULL,
  `SolicitudReposicionid` INT NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `uq_Notificacion_usuario_solicitud` UNIQUE (`Usuarioid`, `SolicitudReposicionid`),
  CONSTRAINT `fk_Notificacion_Usuarioid` FOREIGN KEY (`Usuarioid`) REFERENCES `Usuario` (`id`),
  CONSTRAINT `fk_Notificacion_SolicitudReposicionid` FOREIGN KEY (`SolicitudReposicionid`) REFERENCES `SolicitudReposicion` (`id`)
) ENGINE=InnoDB;

CREATE TABLE `PerfilClinico` (
  `Usuarioid` INT NOT NULL,
  `tipoSanguineo` VARCHAR(2),
  `factorRh` VARCHAR(1),
  `fechaRegistroPerfil` DATE,
  `radioNotificacion` INT NOT NULL DEFAULT 5,
  `latitud` DECIMAL(9,6) COMMENT 'Ubicación del donador para HU-3.2; nunca se expone por la API.',
  `longitud` DECIMAL(9,6) COMMENT 'Ubicación del donador para HU-3.2; nunca se expone por la API.',
  PRIMARY KEY (`Usuarioid`),
  CONSTRAINT `chk_PerfilClinico_tipoSanguineo` CHECK (tipoSanguineo IN ('A','B','AB','O')),
  CONSTRAINT `chk_PerfilClinico_factorRh` CHECK (factorRh IN ('+','-')),
  CONSTRAINT `chk_PerfilClinico_radioNotificacion` CHECK (radioNotificacion IN (3,5,10)),
  CONSTRAINT `fk_PerfilClinico_Usuarioid` FOREIGN KEY (`Usuarioid`) REFERENCES `Usuario` (`id`)
) ENGINE=InnoDB;

CREATE TABLE `RegistroDonacion` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `fechaDonacion` DATE NOT NULL,
  `proximaFechaElegible` DATE COMMENT 'Precalculada: máx(última donación + 56 días, fechaRespuesta + tiempoEsperaDias).',
  `Usuarioid` INT NOT NULL,
  `PersonalCETSUsuarioid` INT NOT NULL,
  `SolicitudReposicionid` INT,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_RegistroDonacion_Usuarioid` FOREIGN KEY (`Usuarioid`) REFERENCES `Usuario` (`id`),
  CONSTRAINT `fk_RegistroDonacion_PersonalCETSUsuarioid` FOREIGN KEY (`PersonalCETSUsuarioid`) REFERENCES `PersonalCETS` (`Usuarioid`),
  CONSTRAINT `fk_RegistroDonacion_SolicitudReposicionid` FOREIGN KEY (`SolicitudReposicionid`) REFERENCES `SolicitudReposicion` (`id`)
) ENGINE=InnoDB;

CREATE TABLE `RespuestaCriterio` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `CuestionarioRespuestaid` INT NOT NULL,
  `CriterioExclusionid` INT NOT NULL,
  `preguntaTexto` VARCHAR(200) NOT NULL COMMENT 'Copia del texto del criterio al responder: editar el criterio no altera respuestas pasadas (HU-4.3).',
  `respuesta` VARCHAR(100) NOT NULL,
  `excluye` BOOLEAN NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_RespuestaCriterio_CuestionarioRespuestaid` FOREIGN KEY (`CuestionarioRespuestaid`) REFERENCES `CuestionarioRespuesta` (`id`),
  CONSTRAINT `fk_RespuestaCriterio_CriterioExclusionid` FOREIGN KEY (`CriterioExclusionid`) REFERENCES `CriterioExclusion` (`id`)
) ENGINE=InnoDB;

CREATE TABLE `UnidadSangre` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `tipoSanguineo` VARCHAR(2) COMMENT 'Tipo confirmado en laboratorio; puede diferir del declarado en PerfilClinico.',
  `factorRh` VARCHAR(1) COMMENT 'Tipo confirmado en laboratorio; puede diferir del declarado en PerfilClinico.',
  `componente` VARCHAR(50),
  `fechaExtraccion` DATE,
  `fechaCaducidad` DATE,
  `estatus` VARCHAR(20),
  `PersonalCETSUsuarioid` INT NOT NULL,
  `SolicitudReposicionid` INT,
  `RegistroDonacionid` INT NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_UnidadSangre_PersonalCETSUsuarioid` FOREIGN KEY (`PersonalCETSUsuarioid`) REFERENCES `PersonalCETS` (`Usuarioid`),
  CONSTRAINT `fk_UnidadSangre_SolicitudReposicionid` FOREIGN KEY (`SolicitudReposicionid`) REFERENCES `SolicitudReposicion` (`id`),
  CONSTRAINT `fk_UnidadSangre_RegistroDonacionid` FOREIGN KEY (`RegistroDonacionid`) REFERENCES `RegistroDonacion` (`id`)
) ENGINE=InnoDB;

