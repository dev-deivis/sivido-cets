-- =====================================================================
-- SIVIDO — datos_prueba.sql
-- Datos de prueba por módulo (TASKS.md, sección 2) para una base `sivido`
-- que YA tiene las 17 tablas. No crea ni borra tablas.
--
-- * Todos los registros usan IDs fijos 901+ para no chocar con datos reales.
-- * Los hospitales solo se insertan si no existe ya uno con el mismo nombre;
--   las solicitudes buscan el hospital por nombre.
-- * Para volver a cargarlo: primero corre sql/limpiar_datos_prueba.sql.
-- * No usa EXIT: se puede correr en el editor SQL de NetBeans.
--
-- Cuentas (SHA-256 sin sal, igual que LnCuentas.hash()):
--   CETS (login institucional): admin@cets.oaxaca.gob.mx / Cets2026!
--   Familiar:                   familiar@prueba.mx       / Donador2026!
--   Donador cercano O-:         donador1@prueba.mx       / Donador2026!
--   Donador lejano A+:          donador2@prueba.mx       / Donador2026!
-- =====================================================================

USE sivido;

START TRANSACTION;

-- ---------------------------------------------------------------------
-- 2.1 Comunes
-- ---------------------------------------------------------------------

-- Catálogo de hospitales (HU-2.1). Coordenadas aproximadas de la ciudad de Oaxaca.
INSERT INTO Hospital (id, nombre, latitud, longitud)
SELECT 901, 'Hospital General "Dr. Aurelio Valdivieso" (Hospital Civil)', 17.070200, -96.718700 FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM Hospital WHERE nombre = 'Hospital General "Dr. Aurelio Valdivieso" (Hospital Civil)');
INSERT INTO Hospital (id, nombre, latitud, longitud)
SELECT 902, 'Hospital Regional de Alta Especialidad de Oaxaca (HRAEO)', 16.963700, -96.712000 FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM Hospital WHERE nombre = 'Hospital Regional de Alta Especialidad de Oaxaca (HRAEO)');
INSERT INTO Hospital (id, nombre, latitud, longitud)
SELECT 903, 'Hospital General de Zona No. 1 IMSS Oaxaca', 17.079000, -96.709000 FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM Hospital WHERE nombre = 'Hospital General de Zona No. 1 IMSS Oaxaca');
INSERT INTO Hospital (id, nombre, latitud, longitud)
SELECT 904, 'Hospital Regional Presidente Juárez ISSSTE', 17.080700, -96.710600 FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM Hospital WHERE nombre = 'Hospital Regional Presidente Juárez ISSSTE');
INSERT INTO Hospital (id, nombre, latitud, longitud)
SELECT 905, 'Hospital de la Niñez Oaxaqueña', 16.970000, -96.718000 FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM Hospital WHERE nombre = 'Hospital de la Niñez Oaxaqueña');

-- Personal CETS (HU-6.x, HU-5.1). Login institucional: admin@cets.oaxaca.gob.mx / Cets2026!
INSERT INTO Usuario (id, nombreCompleto, telefono, correo, passwordHash, fechaRegistro) VALUES
 (901, 'Administrador CETS Prueba', '9510000001', 'personal.cets@prueba.mx',
  'b4eb84f0c1d0ee7d6a1cd590e626a405a50e2a005dc68683543bc93762cd8525', CURDATE());
INSERT INTO PersonalCETS (Usuarioid, rol, correoInstitucional, activo) VALUES
 (901, 'Administrador', 'admin@cets.oaxaca.gob.mx', TRUE);

-- Familiar que crea solicitudes y dos donadores (password: Donador2026!)
INSERT INTO Usuario (id, nombreCompleto, telefono, correo, passwordHash, fechaRegistro) VALUES
 (902, 'Familiar Prueba',    '9510000002', 'familiar@prueba.mx',
  '555d0ba5699947a8381c09a09a0c93e591e12623ccbf5ba7ff6e89f74528b5d4', CURDATE()),
 (903, 'Donador Cercano O-', '9510000003', 'donador1@prueba.mx',
  '555d0ba5699947a8381c09a09a0c93e591e12623ccbf5ba7ff6e89f74528b5d4', CURDATE()),
 (904, 'Donador Lejano A+',  '9510000004', 'donador2@prueba.mx',
  '555d0ba5699947a8381c09a09a0c93e591e12623ccbf5ba7ff6e89f74528b5d4', CURDATE());

-- Perfiles clínicos (PK compartida con Usuario). 903 está a ~1 km del Hospital Civil; 904 está en el Istmo.
INSERT INTO PerfilClinico (Usuarioid, tipoSanguineo, factorRh, fechaRegistroPerfil, radioNotificacion, latitud, longitud) VALUES
 (903, 'O', '-', CURDATE(), 5, 17.065000, -96.720000),
 (904, 'A', '+', CURDATE(), 3, 16.200000, -95.200000);

-- ---------------------------------------------------------------------
-- 2.2 Solicitudes (mientras HU-2.1 no esté lista)
-- ---------------------------------------------------------------------
INSERT INTO SolicitudReposicion
 (id, folioCETS, tipoSanguineo, factorRh, descripcion, estatus, fechaHoraRegistro, uuidPublico, Usuarioid, Hospitalid) VALUES
 (901, 'CETS-OAX-2026-0901', 'O', '-', 'Cirugía programada, se requieren 3 donadores', 'Activa',
  NOW(), UUID(), 902,
  (SELECT id FROM Hospital WHERE nombre = 'Hospital General "Dr. Aurelio Valdivieso" (Hospital Civil)')),
 (902, 'CETS-OAX-2026-0902', 'A', '+', 'Accidente vial', 'Activa',
  NOW() - INTERVAL 1 DAY, UUID(), 902,
  (SELECT id FROM Hospital WHERE nombre = 'Hospital Regional de Alta Especialidad de Oaxaca (HRAEO)')),
 (903, 'CETS-OAX-2026-0903', 'B', '+', 'Solicitud ya atendida', 'Atendida',
  NOW() - INTERVAL 3 DAY, UUID(), 902,
  (SELECT id FROM Hospital WHERE nombre = 'Hospital General de Zona No. 1 IMSS Oaxaca'));

-- ---------------------------------------------------------------------
-- 2.3 Notificaciones y cuestionario
-- ---------------------------------------------------------------------
INSERT INTO CriterioExclusion (id, criterio, tipoRespuesta, activo, tipoExclusion, duracionDias) VALUES
 (901, '¿Te hiciste un tatuaje o perforación en los últimos 12 meses?', 'SiNo', TRUE,  'Temporal',   365),
 (902, '¿Has tenido hepatitis B o C?',                                  'SiNo', TRUE,  'Permanente', NULL),
 (903, '¿Tomaste antibióticos en los últimos 7 días?',                  'SiNo', TRUE,  'Temporal',   7),
 (904, '¿Pesas menos de 50 kg?',                                        'SiNo', FALSE, 'Temporal',   0);

INSERT INTO Notificacion (id, distanciaAproximada, estatus, fechaEnvio, Usuarioid, SolicitudReposicionid) VALUES
 (901, 1.00, 'Enviada', NOW(), 903, 901);

-- ---------------------------------------------------------------------
-- 2.4 Donaciones e historial
-- ---------------------------------------------------------------------
-- 903: donó hace 200 días → ya es elegible.  904: donó hace 20 días → todavía no.
INSERT INTO RegistroDonacion (id, fechaDonacion, proximaFechaElegible, Usuarioid, PersonalCETSUsuarioid, SolicitudReposicionid) VALUES
 (901, CURDATE() - INTERVAL 200 DAY, CURDATE() - INTERVAL 144 DAY, 903, 901, NULL),
 (902, CURDATE() - INTERVAL 20 DAY,  CURDATE() + INTERVAL 36 DAY,  904, 901, 903);

-- 904 tiene un padecimiento con exclusión permanente (criterio 902).
INSERT INTO HistorialClinico (id, diagnostico, fechaDeteccion, observaciones, Usuarioid, PersonalCETSUsuarioid, CriterioExclusionid) VALUES
 (901, 'Hepatitis C', CURDATE() - INTERVAL 1 YEAR, 'Dato de prueba', 904, 901, 902);
-- D10: la fuente única es Usuario.exclusionPermanente.
UPDATE Usuario SET exclusionPermanente = TRUE WHERE id = 904;

-- Historial externo (HU-7.3)
INSERT INTO InstitucionExterna (id, nombreInstitucion, tipoConexion, contactoTecnico, activa) VALUES
 (901, 'Banco de Sangre externo (prueba)', 'API REST', 'soporte@prueba.mx', TRUE);
INSERT INTO HistorialExterno (id, Usuarioid, InstitucionExternaid, fechaDonacion, observaciones) VALUES
 (901, 903, 901, '2025-03-10', 'Donación registrada en otra institución');

COMMIT;

-- Verificación rápida
SELECT 'Usuario' AS tabla, COUNT(*) AS filas FROM Usuario WHERE id IN (901, 902, 903, 904)
UNION ALL SELECT 'SolicitudReposicion', COUNT(*) FROM SolicitudReposicion WHERE id IN (901, 902, 903)
UNION ALL SELECT 'CriterioExclusion',   COUNT(*) FROM CriterioExclusion   WHERE id IN (901, 902, 903, 904)
UNION ALL SELECT 'RegistroDonacion',    COUNT(*) FROM RegistroDonacion    WHERE id IN (901, 902);
