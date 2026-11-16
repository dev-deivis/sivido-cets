-- =====================================================================
-- SIVIDO — limpiar_datos_prueba.sql
-- Borra SOLO los datos de prueba que carga sql/datos_prueba.sql
-- y lo que se haya generado en la app usando esas cuentas de prueba.
--
-- Datos de prueba (IDs fijos, nunca rangos):
--   Usuario            901 (personal CETS), 902 (familiar), 903, 904 (donadores)
--   PersonalCETS       901
--   Hospital           901..905 (solo si ninguna solicitud real los usa)
--   SolicitudReposicion 901, 902, 903 + las que haya creado el usuario 902..904 desde la app
--   CriterioExclusion  901..904
--   InstitucionExterna 901
-- No toca ningún otro registro. Las referencias de datos reales hacia datos
-- de prueba que admiten NULL se ponen en NULL en vez de borrarse.
--
-- No usa EXIT: se puede correr en el editor SQL de NetBeans.
-- =====================================================================

USE sivido;

-- 1) Respuestas de cuestionarios de usuarios/solicitudes de prueba
DELETE FROM RespuestaCriterio
 WHERE CuestionarioRespuestaid IN (
         SELECT id FROM CuestionarioRespuesta
          WHERE Usuarioid IN (901, 902, 903, 904)
             OR SolicitudReposicionid IN (SELECT id FROM SolicitudReposicion
                                           WHERE id IN (901, 902, 903) OR Usuarioid IN (901, 902, 903, 904)))
    OR CriterioExclusionid IN (901, 902, 903, 904);

DELETE FROM CuestionarioRespuesta
 WHERE Usuarioid IN (901, 902, 903, 904)
    OR SolicitudReposicionid IN (SELECT id FROM SolicitudReposicion
                                  WHERE id IN (901, 902, 903) OR Usuarioid IN (901, 902, 903, 904));

-- 2) Intereses y notificaciones
DELETE FROM Interes
 WHERE Usuarioid IN (901, 902, 903, 904)
    OR SolicitudReposicionid IN (SELECT id FROM SolicitudReposicion
                                  WHERE id IN (901, 902, 903) OR Usuarioid IN (901, 902, 903, 904));

DELETE FROM Notificacion
 WHERE Usuarioid IN (901, 902, 903, 904)
    OR SolicitudReposicionid IN (SELECT id FROM SolicitudReposicion
                                  WHERE id IN (901, 902, 903) OR Usuarioid IN (901, 902, 903, 904));

-- 3) Bitácora de estatus
DELETE FROM HistorialEstatusSolicitud
 WHERE PersonalCETSUsuarioid = 901
    OR SolicitudReposicionid IN (SELECT id FROM SolicitudReposicion
                                  WHERE id IN (901, 902, 903) OR Usuarioid IN (901, 902, 903, 904));

-- 4) Unidades de sangre y donaciones
DELETE FROM UnidadSangre
 WHERE PersonalCETSUsuarioid = 901
    OR RegistroDonacionid IN (SELECT id FROM RegistroDonacion
                               WHERE Usuarioid IN (901, 902, 903, 904) OR PersonalCETSUsuarioid = 901)
    OR SolicitudReposicionid IN (SELECT id FROM SolicitudReposicion
                                  WHERE id IN (901, 902, 903) OR Usuarioid IN (901, 902, 903, 904));

-- Donaciones reales ligadas a una solicitud de prueba: solo se desliga (columna nullable)
UPDATE RegistroDonacion SET SolicitudReposicionid = NULL
 WHERE Usuarioid NOT IN (901, 902, 903, 904) AND PersonalCETSUsuarioid <> 901
   AND SolicitudReposicionid IN (SELECT id FROM SolicitudReposicion
                                  WHERE id IN (901, 902, 903) OR Usuarioid IN (901, 902, 903, 904));

DELETE FROM RegistroDonacion
 WHERE Usuarioid IN (901, 902, 903, 904) OR PersonalCETSUsuarioid = 901;

-- 5) Historial clínico (registros reales que usan un criterio de prueba: solo se desligan)
UPDATE HistorialClinico SET CriterioExclusionid = NULL
 WHERE CriterioExclusionid IN (901, 902, 903, 904)
   AND Usuarioid NOT IN (901, 902, 903, 904) AND PersonalCETSUsuarioid <> 901;

DELETE FROM HistorialClinico
 WHERE Usuarioid IN (901, 902, 903, 904) OR PersonalCETSUsuarioid = 901;

-- 6) Interoperabilidad
DELETE FROM HistorialExterno   WHERE Usuarioid IN (901, 902, 903, 904) OR InstitucionExternaid = 901;
DELETE FROM IntercambioExterno WHERE Usuarioid IN (901, 902, 903, 904) OR InstitucionExternaid = 901;
DELETE FROM InstitucionExterna WHERE id = 901;

-- 7) Solicitudes (las reales atendidas por el personal de prueba solo se desligan)
UPDATE SolicitudReposicion SET PersonalCETSUsuarioid = NULL
 WHERE PersonalCETSUsuarioid = 901
   AND id NOT IN (901, 902, 903) AND Usuarioid NOT IN (901, 902, 903, 904);

DELETE FROM SolicitudReposicion
 WHERE id IN (901, 902, 903) OR Usuarioid IN (901, 902, 903, 904);

-- 8) Catálogos de prueba
DELETE FROM CriterioExclusion WHERE id IN (901, 902, 903, 904);

DELETE FROM Hospital
 WHERE id IN (901, 902, 903, 904, 905)
   AND id NOT IN (SELECT Hospitalid FROM SolicitudReposicion);

-- 9) Cuentas de prueba
DELETE FROM PerfilClinico WHERE Usuarioid IN (901, 902, 903, 904);
DELETE FROM PersonalCETS  WHERE Usuarioid = 901;
DELETE FROM Usuario       WHERE id IN (901, 902, 903, 904);
