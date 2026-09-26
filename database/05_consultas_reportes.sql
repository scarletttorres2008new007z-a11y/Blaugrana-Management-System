/*
    BLAUGRANA MANAGEMENT SYSTEM
    Script 05 - Consultas de referencia para reportes
    Motor: Microsoft SQL Server

    Estas consultas sirven como referencia de las mismas operaciones que
    ejecuta la capa Java (paquete dao / service) y pueden usarse para
    validar manualmente los datos que muestra la aplicacion.
*/

USE BlaugranaDB;
GO

-- 1) Reporte de plantilla: jugadores activos agrupados por posicion
SELECT posicion, COUNT(*) AS total_jugadores
FROM JUGADOR
WHERE estado = 'ACTIVO'
GROUP BY posicion
ORDER BY posicion;
GO

-- 2) Reporte de contratos vigentes con datos del jugador
SELECT j.numero_camiseta, j.nombre, j.apellido, c.fecha_inicio, c.fecha_fin,
       c.salario_base, c.estado
FROM CONTRATO c
JOIN JUGADOR j ON j.id_jugador = c.id_jugador
WHERE c.estado = 'VIGENTE'
ORDER BY j.numero_camiseta;
GO

-- 3) Reporte deportivo: resultados de partidos finalizados
SELECT competicion, fecha, rival, condicion, goles_favor, goles_contra, resultado
FROM PARTIDO
WHERE estado = 'FINALIZADO'
ORDER BY fecha DESC;
GO

-- 4) Reporte de rendimiento acumulado por jugador
SELECT j.numero_camiseta, j.nombre, j.apellido,
       SUM(pp.minutos_jugados) AS minutos_totales,
       SUM(pp.goles)           AS goles_totales,
       SUM(pp.asistencias)     AS asistencias_totales
FROM PARTICIPACION_PARTIDO pp
JOIN JUGADOR j ON j.id_jugador = pp.id_jugador
GROUP BY j.numero_camiseta, j.nombre, j.apellido
ORDER BY goles_totales DESC, asistencias_totales DESC;
GO

-- 5) Reporte de bonificaciones totales por jugador
SELECT j.numero_camiseta, j.nombre, j.apellido, SUM(b.valor) AS total_bonificaciones
FROM BONIFICACION b
JOIN JUGADOR j ON j.id_jugador = b.id_jugador
GROUP BY j.numero_camiseta, j.nombre, j.apellido
ORDER BY total_bonificaciones DESC;
GO

-- 6) Reporte de pagos por periodo
SELECT periodo, j.nombre, j.apellido, salario_base, bonificaciones, deducciones, total, estado
FROM PAGO p
JOIN JUGADOR j ON j.id_jugador = p.id_jugador
ORDER BY periodo DESC, total DESC;
GO

-- 7) Reporte financiero: ingresos por categoria
SELECT ci.nombre AS categoria, SUM(i.monto) AS total
FROM INGRESO i
JOIN CATEGORIA_INGRESO ci ON ci.id_categoria_ingreso = i.id_categoria_ingreso
GROUP BY ci.nombre
ORDER BY total DESC;
GO

-- 8) Reporte financiero: egresos por categoria
SELECT ce.nombre AS categoria, SUM(e.monto) AS total
FROM EGRESO e
JOIN CATEGORIA_EGRESO ce ON ce.id_categoria_egreso = e.id_categoria_egreso
GROUP BY ce.nombre
ORDER BY total DESC;
GO

-- 9) Balance general (ingresos - egresos)
SELECT
    (SELECT ISNULL(SUM(monto), 0) FROM INGRESO) AS total_ingresos,
    (SELECT ISNULL(SUM(monto), 0) FROM EGRESO)  AS total_egresos,
    (SELECT ISNULL(SUM(monto), 0) FROM INGRESO) - (SELECT ISNULL(SUM(monto), 0) FROM EGRESO) AS balance;
GO

-- 10) Reporte presupuestario: presupuestado vs ejecutado
SELECT pr.temporada, dp.categoria, dp.monto_presupuestado, dp.monto_ejecutado,
       (dp.monto_presupuestado - dp.monto_ejecutado) AS disponible
FROM DETALLE_PRESUPUESTO dp
JOIN PRESUPUESTO pr ON pr.id_presupuesto = dp.id_presupuesto
ORDER BY pr.temporada, dp.categoria;
GO
