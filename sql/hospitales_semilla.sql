-- Catálogo inicial de hospitales para HU-2.1 (selector de hospital al capturar una solicitud).
-- Coordenadas aproximadas (Oaxaca de Juárez y zona conurbada). Ejecutar una sola vez en la BD sivido.
USE sivido;

INSERT INTO Hospital (nombre, latitud, longitud)
SELECT * FROM (
    SELECT 'Hospital General Dr. Aurelio Valdivieso (Hospital Civil)' AS nombre, 17.0732 AS latitud, -96.7166 AS longitud UNION ALL
    SELECT 'Hospital Regional de Alta Especialidad de Oaxaca (HRAEO)', 16.9998, -96.7530 UNION ALL
    SELECT 'Hospital General de Zona No. 1 IMSS Oaxaca', 17.0757, -96.7128 UNION ALL
    SELECT 'Hospital Regional Presidente Juárez ISSSTE', 17.0869, -96.7146 UNION ALL
    SELECT 'Hospital de la Niñez Oaxaqueña', 16.9932, -96.7497
) AS nuevos
WHERE NOT EXISTS (SELECT 1 FROM Hospital h WHERE h.nombre = nuevos.nombre);
