/*
    BLAUGRANA MANAGEMENT SYSTEM
    Script 04 - Datos de demostracion

    IMPORTANTE:
    Estos registros son UNICAMENTE datos de prueba para un proyecto academico.
    Los nombres de jugadores, cuerpo tecnico y cifras economicas se usan con
    fines ilustrativos de un club de futbol ficticio inspirado en el FC
    Barcelona; los montos de salarios, ingresos y egresos son inventados y
    no representan cifras reales del club.
*/

USE BlaugranaDB;
GO

-- =========================================================
-- ROLES Y USUARIO ADMINISTRADOR
-- =========================================================

INSERT INTO ROL (nombre_rol, descripcion) VALUES
    (N'Administrador',       N'Acceso total al sistema'),
    (N'Directivo',           N'Consulta general de club, deportivo y finanzas'),
    (N'Gestor Deportivo',    N'Gestion de jugadores, partidos y rendimiento'),
    (N'Gestor Financiero',   N'Gestion de pagos, ingresos, egresos y presupuesto');
GO

-- Usuario: admin  /  Contrasena: admin123  (hash SHA-256)
INSERT INTO USUARIO (nombre_usuario, contrasena_hash, id_rol, nombre_completo, correo, estado)
SELECT N'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
       id_rol, N'Administrador del Sistema', N'admin@blaugrana-management.local', 'ACTIVO'
FROM ROL WHERE nombre_rol = N'Administrador';
GO

-- =========================================================
-- PLANTILLA DE JUGADORES (datos de demostracion)
-- =========================================================

INSERT INTO JUGADOR (nombre, apellido, documento, fecha_nacimiento, nacionalidad, posicion, numero_camiseta, fecha_ingreso, estado) VALUES
    (N'Joan',     N'García',          N'DOC-0001', '1997-05-04', N'España',        N'Portero',         1,  '2023-07-01', 'ACTIVO'),
    (N'Wojciech', N'Szczęsny',        N'DOC-0002', '1990-04-18', N'Polonia',       N'Portero',         13, '2023-08-15', 'ACTIVO'),
    (N'Dominik',  N'Livaković',       N'DOC-0003', '1995-01-09', N'Croacia',       N'Portero',         25, '2024-07-01', 'ACTIVO'),
    (N'João',     N'Cancelo',         N'DOC-0004', '1994-05-27', N'Portugal',      N'Defensa',         2,  '2023-09-01', 'ACTIVO'),
    (N'Alejandro',N'Balde',           N'DOC-0005', '2003-10-18', N'España',        N'Defensa',         3,  '2020-07-01', 'ACTIVO'),
    (N'Pau',      N'Cubarsí',         N'DOC-0006', '2007-01-22', N'España',        N'Defensa',         5,  '2024-07-01', 'ACTIVO'),
    (N'Andreas',  N'Christensen',     N'DOC-0007', '1996-04-10', N'Dinamarca',     N'Defensa',         15, '2022-07-01', 'ACTIVO'),
    (N'Gerard',   N'Martín',          N'DOC-0008', '2003-02-01', N'España',        N'Defensa',         18, '2023-07-01', 'ACTIVO'),
    (N'Jules',    N'Kounde',          N'DOC-0009', '1998-11-12', N'Francia',       N'Defensa',         23, '2022-08-01', 'ACTIVO'),
    (N'Eric',     N'Garcia',          N'DOC-0010', '2001-01-09', N'España',        N'Defensa',         24, '2021-07-01', 'ACTIVO'),
    (N'Brian',    N'Fariñas',         N'DOC-0011', '2006-06-06', N'España',        N'Centrocampista',  4,  '2025-07-01', 'ACTIVO'),
    (N'Gavi',     N'',                N'DOC-0012', '2004-08-05', N'España',        N'Centrocampista',  6,  '2021-07-01', 'ACTIVO'),
    (N'Fermín',   N'López',           N'DOC-0013', '2002-05-11', N'España',        N'Centrocampista',  7,  '2022-07-01', 'ACTIVO'),
    (N'Pedri',    N'',                N'DOC-0014', '2002-11-25', N'España',        N'Centrocampista',  8,  '2020-08-01', 'ACTIVO'),
    (N'Rodrigo',  N'',                N'DOC-0015', '2005-06-01', N'Brasil',        N'Centrocampista',  16, '2025-01-15', 'ACTIVO'),
    (N'Dani',     N'Olmo',            N'DOC-0016', '1998-05-07', N'España',        N'Centrocampista',  20, '2024-07-01', 'ACTIVO'),
    (N'Frenkie',  N'de Jong',         N'DOC-0017', '1997-05-12', N'Países Bajos',  N'Centrocampista',  21, '2019-07-01', 'ACTIVO'),
    (N'Marc',     N'Bernal',          N'DOC-0018', '2006-01-15', N'España',        N'Centrocampista',  22, '2024-07-01', 'ACTIVO'),
    (N'Gabriel',  N'Jesus',           N'DOC-0019', '1997-04-03', N'Brasil',        N'Delantero',       9,  '2026-07-01', 'ACTIVO'),
    (N'Lamine',   N'Yamal',           N'DOC-0020', '2007-07-13', N'España',        N'Delantero',       10, '2023-06-01', 'ACTIVO'),
    (N'Raphinha', N'',                N'DOC-0021', '1996-12-14', N'Brasil',        N'Delantero',       11, '2022-07-01', 'ACTIVO'),
    (N'Karim',    N'Adeyemi',         N'DOC-0022', '2002-01-18', N'Alemania',      N'Delantero',       14, '2026-07-01', 'ACTIVO'),
    (N'Anthony',  N'Gordon',          N'DOC-0023', '2001-02-24', N'Inglaterra',    N'Delantero',       17, '2026-07-01', 'ACTIVO'),
    (N'Roony',    N'Bardghji',        N'DOC-0024', '2006-01-02', N'Suecia',        N'Delantero',       19, '2024-07-01', 'ACTIVO');
GO

-- =========================================================
-- CUERPO TECNICO Y PERSONAL DEL CLUB (datos de demostracion)
-- =========================================================

INSERT INTO PERSONAL (nombre, apellido, cargo, area, nacionalidad, fecha_ingreso, estado) VALUES
    (N'Hansi',      N'Flick',           N'Entrenador Principal',                              N'Cuerpo Técnico', N'Alemania', '2024-07-01', 'ACTIVO'),
    (N'Marcus',     N'Sorg',            N'Entrenador Asistente',                               N'Cuerpo Técnico', N'Alemania', '2024-07-01', 'ACTIVO'),
    (N'Toni',       N'Tapalovic',       N'Entrenador Asistente',                               N'Cuerpo Técnico', N'Alemania', '2024-07-01', 'ACTIVO'),
    (N'Heiko',      N'Westermann',      N'Entrenador Asistente',                               N'Cuerpo Técnico', N'Alemania', '2024-07-01', 'ACTIVO'),
    (N'José Ramón', N'de la Fuente',    N'Entrenador de Porteros',                             N'Cuerpo Técnico', N'España',   '2018-07-01', 'ACTIVO'),
    (N'Pepe',       N'Conde',           N'Preparador Físico de Campo',                         N'Cuerpo Técnico', N'España',   '2020-07-01', 'ACTIVO'),
    (N'Benjamin',   N'Kugel',           N'Preparador Físico de Gimnasio y Fuerza',             N'Cuerpo Técnico', N'Alemania', '2024-07-01', 'ACTIVO');
GO

-- =========================================================
-- CONTRATOS (montos ficticios con fines academicos)
-- =========================================================

INSERT INTO CONTRATO (id_jugador, fecha_inicio, fecha_fin, salario_base, condiciones, estado)
SELECT j.id_jugador, v.fecha_inicio, v.fecha_fin, v.salario_base,
       N'Contrato profesional estandar con clausula de rescision', 'VIGENTE'
FROM JUGADOR j
JOIN (VALUES
    (1,  '2023-07-01', '2028-06-30', 300000.00),
    (13, '2023-08-15', '2027-06-30', 250000.00),
    (25, '2024-07-01', '2027-06-30', 200000.00),
    (2,  '2023-09-01', '2027-06-30', 400000.00),
    (3,  '2020-07-01', '2028-06-30', 450000.00),
    (5,  '2024-07-01', '2029-06-30', 400000.00),
    (15, '2022-07-01', '2027-06-30', 350000.00),
    (18, '2023-07-01', '2027-06-30', 250000.00),
    (23, '2022-08-01', '2027-06-30', 450000.00),
    (24, '2021-07-01', '2027-06-30', 300000.00),
    (4,  '2025-07-01', '2029-06-30', 200000.00),
    (6,  '2021-07-01', '2029-06-30', 450000.00),
    (7,  '2022-07-01', '2029-06-30', 400000.00),
    (8,  '2020-08-01', '2030-06-30', 500000.00),
    (16, '2025-01-15', '2029-06-30', 350000.00),
    (20, '2024-07-01', '2029-06-30', 500000.00),
    (21, '2019-07-01', '2027-06-30', 550000.00),
    (22, '2024-07-01', '2029-06-30', 250000.00),
    (9,  '2026-07-01', '2029-06-30', 450000.00),
    (10, '2023-06-01', '2031-06-30', 600000.00),
    (11, '2022-07-01', '2027-06-30', 480000.00),
    (14, '2026-07-01', '2030-06-30', 400000.00),
    (17, '2026-07-01', '2030-06-30', 400000.00),
    (19, '2024-07-01', '2029-06-30', 200000.00)
) AS v(numero_camiseta, fecha_inicio, fecha_fin, salario_base)
ON j.numero_camiseta = v.numero_camiseta;
GO

-- =========================================================
-- PARTIDOS (datos de demostracion)
-- =========================================================

INSERT INTO PARTIDO (competicion, fecha, rival, condicion, goles_favor, goles_contra, resultado, estado) VALUES
    (N'LaLiga',                   '2026-08-16', N'Rayo Vallecano',      'LOCAL',      3, 1, 'GANADO', 'FINALIZADO'),
    (N'UEFA Champions League',    '2026-09-17', N'Manchester City',     'VISITANTE',  2, 1, 'GANADO', 'FINALIZADO'),
    (N'UEFA Champions League',    '2026-09-23', N'Paris Saint-Germain', 'LOCAL',      2, 1, 'GANADO', 'FINALIZADO'),
    (N'Copa del Rey',             '2026-01-10', N'Athletic Club',       'VISITANTE',  2, 0, 'GANADO', 'FINALIZADO'),
    (N'LaLiga',                   '2026-10-04', N'Real Madrid',         'LOCAL',      0, 0, NULL,     'PROGRAMADO');
GO

-- =========================================================
-- PARTICIPACION EN PARTIDOS (datos de demostracion)
-- =========================================================

INSERT INTO PARTICIPACION_PARTIDO (id_partido, id_jugador, minutos_jugados, goles, asistencias, tarjetas_amarillas, tarjetas_rojas, titular)
SELECT p.id_partido, j.id_jugador, v.minutos, v.goles, v.asistencias, v.amarillas, v.rojas, v.titular
FROM PARTIDO p
JOIN JUGADOR j ON 1 = 1
JOIN (VALUES
    -- FC Barcelona 2 - 1 Paris Saint-Germain (UEFA Champions League, 2026-09-23)
    (N'UEFA Champions League', N'Paris Saint-Germain', 10, 90, 1, 1, 0, 0, 1),
    (N'UEFA Champions League', N'Paris Saint-Germain', 11, 82, 1, 0, 0, 0, 1),
    (N'UEFA Champions League', N'Paris Saint-Germain', 8,  90, 0, 1, 0, 0, 1),
    (N'UEFA Champions League', N'Paris Saint-Germain', 9,  75, 0, 0, 0, 0, 1),
    -- FC Barcelona 3 - 1 Rayo Vallecano (LaLiga, 2026-08-16)
    (N'LaLiga',                N'Rayo Vallecano',      10, 90, 2, 0, 0, 0, 1),
    (N'LaLiga',                N'Rayo Vallecano',      20, 70, 1, 1, 1, 0, 1),
    (N'LaLiga',                N'Rayo Vallecano',      6,  90, 0, 1, 0, 0, 1)
) AS v(competicion, rival, numero_camiseta, minutos, goles, asistencias, amarillas, rojas, titular)
    ON p.competicion = v.competicion AND p.rival = v.rival AND j.numero_camiseta = v.numero_camiseta;
GO

-- =========================================================
-- BONIFICACIONES (generadas a partir del rendimiento anterior)
-- =========================================================

INSERT INTO BONIFICACION (id_jugador, id_partido, concepto, valor, fecha_generacion)
SELECT j.id_jugador, p.id_partido, v.concepto, v.valor, p.fecha
FROM PARTIDO p
JOIN JUGADOR j ON 1 = 1
JOIN (VALUES
    (N'UEFA Champions League', N'Paris Saint-Germain', 10, N'Gol convertido',        500.00),
    (N'UEFA Champions League', N'Paris Saint-Germain', 10, N'Asistencia',            250.00),
    (N'UEFA Champions League', N'Paris Saint-Germain', 10, N'Victoria del equipo',  1000.00),
    (N'UEFA Champions League', N'Paris Saint-Germain', 11, N'Gol convertido',        500.00),
    (N'UEFA Champions League', N'Paris Saint-Germain', 11, N'Victoria del equipo',  1000.00),
    (N'UEFA Champions League', N'Paris Saint-Germain', 8,  N'Asistencia',            250.00),
    (N'UEFA Champions League', N'Paris Saint-Germain', 8,  N'Victoria del equipo',  1000.00)
) AS v(competicion, rival, numero_camiseta, concepto, valor)
    ON p.competicion = v.competicion AND p.rival = v.rival AND j.numero_camiseta = v.numero_camiseta;
GO

-- =========================================================
-- PAGOS (periodo 2026-09)
-- =========================================================

INSERT INTO PAGO (id_jugador, id_contrato, periodo, salario_base, bonificaciones, deducciones, total, fecha_pago, estado)
SELECT j.id_jugador, c.id_contrato, N'2026-09', v.salario_base, v.bonificaciones, v.deducciones, v.total, v.fecha_pago, v.estado
FROM JUGADOR j
JOIN CONTRATO c ON c.id_jugador = j.id_jugador
JOIN (VALUES
    (10, 50000.00, 1750.00, 2000.00, 49750.00, NULL,         'PENDIENTE'),
    (11, 40000.00, 1500.00, 1500.00, 40000.00, '2026-09-05', 'PAGADO')
) AS v(numero_camiseta, salario_base, bonificaciones, deducciones, total, fecha_pago, estado)
    ON j.numero_camiseta = v.numero_camiseta;
GO

-- =========================================================
-- INGRESOS
-- =========================================================

INSERT INTO CATEGORIA_INGRESO (nombre) VALUES
    (N'Patrocinios'), (N'Entradas'), (N'Merchandising'), (N'Premios');
GO

INSERT INTO INGRESO (id_categoria_ingreso, descripcion, monto, fecha)
SELECT ci.id_categoria_ingreso, v.descripcion, v.monto, v.fecha
FROM CATEGORIA_INGRESO ci
JOIN (VALUES
    (N'Patrocinios',   N'Patrocinio principal de camiseta', 50000.00, '2026-09-01'),
    (N'Entradas',      N'Venta de entradas partidos locales', 25000.00, '2026-09-10'),
    (N'Merchandising', N'Venta de productos oficiales',       20000.00, '2026-09-15'),
    (N'Premios',       N'Premio por clasificacion en competicion', 30000.00, '2026-09-20')
) AS v(categoria, descripcion, monto, fecha) ON ci.nombre = v.categoria;
GO

-- =========================================================
-- EGRESOS
-- =========================================================

INSERT INTO CATEGORIA_EGRESO (nombre) VALUES
    (N'Salarios'), (N'Equipamiento'), (N'Transporte'), (N'Mantenimiento'), (N'Otros');
GO

INSERT INTO EGRESO (id_categoria_egreso, descripcion, monto, fecha)
SELECT ce.id_categoria_egreso, v.descripcion, v.monto, v.fecha
FROM CATEGORIA_EGRESO ce
JOIN (VALUES
    (N'Salarios',      N'Planilla de salarios de plantilla', 45000.00, '2026-09-05'),
    (N'Equipamiento',  N'Compra de material deportivo',       15000.00, '2026-09-08'),
    (N'Transporte',    N'Traslados a partidos de visitante',  10000.00, '2026-09-12'),
    (N'Mantenimiento', N'Mantenimiento de instalaciones',      7000.00, '2026-09-18'),
    (N'Otros',         N'Gastos administrativos generales',   10000.00, '2026-09-22')
) AS v(categoria, descripcion, monto, fecha) ON ce.nombre = v.categoria;
GO

-- =========================================================
-- PRESUPUESTO DE TEMPORADA
-- =========================================================

INSERT INTO PRESUPUESTO (temporada, nombre) VALUES
    (N'2026/2027', N'Presupuesto Temporada 2026/2027');
GO

INSERT INTO DETALLE_PRESUPUESTO (id_presupuesto, categoria, monto_presupuestado, monto_ejecutado)
SELECT pr.id_presupuesto, v.categoria, v.presupuestado, v.ejecutado
FROM PRESUPUESTO pr
JOIN (VALUES
    (N'Salarios',      60000.00, 45000.00),
    (N'Equipamiento',  20000.00, 15000.00),
    (N'Transporte',    15000.00, 10000.00),
    (N'Mantenimiento', 10000.00,  7000.00)
) AS v(categoria, presupuestado, ejecutado) ON pr.temporada = N'2026/2027';
GO
