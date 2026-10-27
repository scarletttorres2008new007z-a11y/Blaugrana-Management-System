/*
    BLAUGRANA MANAGEMENT SYSTEM
    Script 09 - Limpieza de duplicados existentes (ejecutar UNA SOLA VEZ)

    Este script NO es parte de la instalacion estandar (01-08, 10). Ejecutalo
    UNICAMENTE si tu base de datos ya tiene datos duplicados porque
    04_datos_demo.sql se corrio mas de una vez antes de que existieran las
    restricciones de los scripts 06, 08 y 10.

    Limpia, en este orden (hijos antes que padres, para no romper llaves
    foraneas):
      1. BONIFICACION  (duplicados por mismo partido + mismo concepto)
      2. BONIFICACION  (huerfanas de partidos duplicados que se van a borrar)
      3. PARTIDO       (duplicados por competicion/fecha/rival/condicion)
      4. PERSONAL      (duplicados por nombre/apellido/cargo/area/ingreso)
      5. INGRESO       (duplicados por categoria/descripcion/monto/fecha)
      6. EGRESO        (duplicados por categoria/descripcion/monto/fecha)
      7. DETALLE_PRESUPUESTO (duplicados dentro del mismo presupuesto)
      8. DETALLE_PRESUPUESTO (huerfanos de presupuestos duplicados)
      9. PRESUPUESTO   (duplicados por temporada/nombre)

    Es seguro volver a ejecutarlo: si ya no hay duplicados, cada paso no
    borra nada.
*/

USE BlaugranaDB;
GO

-- =========================================================
-- 1. BONIFICACION: colapsar copias identicas del mismo partido
-- =========================================================
WITH DupBonificacion AS (
    SELECT id_bonificacion,
           ROW_NUMBER() OVER (
               PARTITION BY id_jugador, id_partido, concepto, valor, fecha_generacion
               ORDER BY id_bonificacion
           ) AS fila
    FROM BONIFICACION
)
DELETE FROM BONIFICACION
WHERE id_bonificacion IN (SELECT id_bonificacion FROM DupBonificacion WHERE fila > 1);
GO

-- =========================================================
-- 2. BONIFICACION: borrar las que quedaran huerfanas al limpiar PARTIDO
-- =========================================================
WITH DupPartido AS (
    SELECT id_partido,
           ROW_NUMBER() OVER (
               PARTITION BY competicion, fecha, rival, condicion
               ORDER BY id_partido
           ) AS fila
    FROM PARTIDO
)
DELETE FROM BONIFICACION
WHERE id_partido IN (SELECT id_partido FROM DupPartido WHERE fila > 1);
GO

-- (PARTICIPACION_PARTIDO no se toca: ya esta protegida por
-- UQ_PARTICIPACION_PARTIDO_JUGADOR y no puede tener duplicados reales)
WITH DupPartido AS (
    SELECT id_partido,
           ROW_NUMBER() OVER (
               PARTITION BY competicion, fecha, rival, condicion
               ORDER BY id_partido
           ) AS fila
    FROM PARTIDO
)
DELETE FROM PARTICIPACION_PARTIDO
WHERE id_partido IN (SELECT id_partido FROM DupPartido WHERE fila > 1);
GO

-- =========================================================
-- 3. PARTIDO: dejar solo el mas antiguo de cada grupo repetido
-- =========================================================
WITH DupPartido AS (
    SELECT id_partido,
           ROW_NUMBER() OVER (
               PARTITION BY competicion, fecha, rival, condicion
               ORDER BY id_partido
           ) AS fila
    FROM PARTIDO
)
DELETE FROM PARTIDO
WHERE id_partido IN (SELECT id_partido FROM DupPartido WHERE fila > 1);
GO

-- =========================================================
-- 4. PERSONAL: dejar solo el mas antiguo de cada grupo repetido
-- =========================================================
WITH DupPersonal AS (
    SELECT id_personal,
           ROW_NUMBER() OVER (
               PARTITION BY nombre, apellido, cargo, area, fecha_ingreso
               ORDER BY id_personal
           ) AS fila
    FROM PERSONAL
)
DELETE FROM PERSONAL
WHERE id_personal IN (SELECT id_personal FROM DupPersonal WHERE fila > 1);
GO

-- =========================================================
-- 5. INGRESO: dejar solo el mas antiguo de cada grupo repetido
-- =========================================================
WITH DupIngreso AS (
    SELECT id_ingreso,
           ROW_NUMBER() OVER (
               PARTITION BY id_categoria_ingreso, descripcion, monto, fecha
               ORDER BY id_ingreso
           ) AS fila
    FROM INGRESO
)
DELETE FROM INGRESO
WHERE id_ingreso IN (SELECT id_ingreso FROM DupIngreso WHERE fila > 1);
GO

-- =========================================================
-- 6. EGRESO: dejar solo el mas antiguo de cada grupo repetido
-- =========================================================
WITH DupEgreso AS (
    SELECT id_egreso,
           ROW_NUMBER() OVER (
               PARTITION BY id_categoria_egreso, descripcion, monto, fecha
               ORDER BY id_egreso
           ) AS fila
    FROM EGRESO
)
DELETE FROM EGRESO
WHERE id_egreso IN (SELECT id_egreso FROM DupEgreso WHERE fila > 1);
GO

-- =========================================================
-- 7. DETALLE_PRESUPUESTO: colapsar copias identicas del mismo presupuesto
-- =========================================================
WITH DupDetalle AS (
    SELECT id_detalle,
           ROW_NUMBER() OVER (
               PARTITION BY id_presupuesto, categoria, monto_presupuestado, monto_ejecutado
               ORDER BY id_detalle
           ) AS fila
    FROM DETALLE_PRESUPUESTO
)
DELETE FROM DETALLE_PRESUPUESTO
WHERE id_detalle IN (SELECT id_detalle FROM DupDetalle WHERE fila > 1);
GO

-- =========================================================
-- 8. DETALLE_PRESUPUESTO: borrar los que quedaran huerfanos al limpiar
--    PRESUPUESTO
-- =========================================================
WITH DupPresupuesto AS (
    SELECT id_presupuesto,
           ROW_NUMBER() OVER (
               PARTITION BY temporada, nombre
               ORDER BY id_presupuesto
           ) AS fila
    FROM PRESUPUESTO
)
DELETE FROM DETALLE_PRESUPUESTO
WHERE id_presupuesto IN (SELECT id_presupuesto FROM DupPresupuesto WHERE fila > 1);
GO

-- =========================================================
-- 9. PRESUPUESTO: dejar solo el mas antiguo de cada grupo repetido
-- =========================================================
WITH DupPresupuesto AS (
    SELECT id_presupuesto,
           ROW_NUMBER() OVER (
               PARTITION BY temporada, nombre
               ORDER BY id_presupuesto
           ) AS fila
    FROM PRESUPUESTO
)
DELETE FROM PRESUPUESTO
WHERE id_presupuesto IN (SELECT id_presupuesto FROM DupPresupuesto WHERE fila > 1);
GO
