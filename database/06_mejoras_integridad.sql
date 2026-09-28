/*
    BLAUGRANA MANAGEMENT SYSTEM
    Script 06 - Mejoras de integridad, catalogos y auditoria (Prioridad 1)
    Motor: Microsoft SQL Server

    Este script se ejecuta DESPUES de 01-04 (sobre una base de datos
    BlaugranaDB ya creada y con los datos de demostracion cargados). No
    modifica ninguna tabla existente en su forma ni requiere cambios en la
    aplicacion Java: solo agrega validaciones, indices y catalogos nuevos.

    Incluye:
      1. CHECK constraints contra datos invalidos.
      2. UNIQUE para evitar participaciones duplicadas por partido/jugador.
      3. UNIQUE para evitar pagos duplicados por jugador/periodo.
      4. Indice filtrado para impedir dos contratos VIGENTE simultaneos
         para el mismo jugador.
      5. Indices en columnas de consulta frecuente.
      6. Tabla AUDITORIA.
      7. Tabla TEMPORADA.
      8. Catalogo de POSICION (de referencia; JUGADOR.posicion se valida
         mientras tanto con un CHECK contra los mismos 4 valores).
*/

USE BlaugranaDB;
GO

-- =========================================================
-- 1. CHECK constraints contra datos invalidos
-- =========================================================

ALTER TABLE JUGADOR ADD CONSTRAINT CK_JUGADOR_CAMISETA CHECK (numero_camiseta BETWEEN 1 AND 99);
ALTER TABLE JUGADOR ADD CONSTRAINT UQ_JUGADOR_CAMISETA UNIQUE (numero_camiseta);
ALTER TABLE JUGADOR ADD CONSTRAINT CK_JUGADOR_ESTADO CHECK (estado IN ('ACTIVO', 'INACTIVO'));
ALTER TABLE JUGADOR ADD CONSTRAINT CK_JUGADOR_POSICION
    CHECK (posicion IN (N'Portero', N'Defensa', N'Centrocampista', N'Delantero'));
GO

ALTER TABLE PERSONAL ADD CONSTRAINT CK_PERSONAL_ESTADO CHECK (estado IN ('ACTIVO', 'INACTIVO'));
GO

ALTER TABLE CONTRATO ADD CONSTRAINT CK_CONTRATO_SALARIO CHECK (salario_base > 0);
ALTER TABLE CONTRATO ADD CONSTRAINT CK_CONTRATO_FECHAS CHECK (fecha_fin > fecha_inicio);
ALTER TABLE CONTRATO ADD CONSTRAINT CK_CONTRATO_ESTADO CHECK (estado IN ('VIGENTE', 'FINALIZADO', 'RESCINDIDO'));
GO

ALTER TABLE PARTIDO ADD CONSTRAINT CK_PARTIDO_GOLES CHECK (goles_favor >= 0 AND goles_contra >= 0);
ALTER TABLE PARTIDO ADD CONSTRAINT CK_PARTIDO_CONDICION CHECK (condicion IN ('LOCAL', 'VISITANTE'));
ALTER TABLE PARTIDO ADD CONSTRAINT CK_PARTIDO_ESTADO CHECK (estado IN ('PROGRAMADO', 'FINALIZADO'));
ALTER TABLE PARTIDO ADD CONSTRAINT CK_PARTIDO_RESULTADO
    CHECK (resultado IS NULL OR resultado IN ('GANADO', 'PERDIDO', 'EMPATADO'));
GO

ALTER TABLE PARTICIPACION_PARTIDO ADD CONSTRAINT CK_PARTICIPACION_VALORES CHECK (
    minutos_jugados BETWEEN 0 AND 130
    AND goles >= 0
    AND asistencias >= 0
    AND tarjetas_amarillas BETWEEN 0 AND 2
    AND tarjetas_rojas BETWEEN 0 AND 1
);
GO

ALTER TABLE BONIFICACION ADD CONSTRAINT CK_BONIFICACION_VALOR CHECK (valor > 0);
GO

ALTER TABLE PAGO ADD CONSTRAINT CK_PAGO_MONTOS
    CHECK (salario_base >= 0 AND bonificaciones >= 0 AND deducciones >= 0);
ALTER TABLE PAGO ADD CONSTRAINT CK_PAGO_ESTADO CHECK (estado IN ('PENDIENTE', 'PAGADO'));
ALTER TABLE PAGO ADD CONSTRAINT CK_PAGO_TOTAL
    CHECK (total = salario_base + bonificaciones - deducciones);
GO

ALTER TABLE INGRESO ADD CONSTRAINT CK_INGRESO_MONTO CHECK (monto > 0);
ALTER TABLE EGRESO ADD CONSTRAINT CK_EGRESO_MONTO CHECK (monto > 0);
GO

ALTER TABLE DETALLE_PRESUPUESTO ADD CONSTRAINT CK_DETALLE_MONTOS
    CHECK (monto_presupuestado >= 0 AND monto_ejecutado >= 0);
GO

ALTER TABLE USUARIO ADD CONSTRAINT CK_USUARIO_ESTADO CHECK (estado IN ('ACTIVO', 'INACTIVO'));
GO

-- =========================================================
-- 2. Evitar participaciones duplicadas (mismo partido + mismo jugador)
-- =========================================================
ALTER TABLE PARTICIPACION_PARTIDO
    ADD CONSTRAINT UQ_PARTICIPACION_PARTIDO_JUGADOR UNIQUE (id_partido, id_jugador);
GO

-- =========================================================
-- 3. Evitar pagos duplicados (mismo jugador + mismo periodo)
-- =========================================================
ALTER TABLE PAGO
    ADD CONSTRAINT UQ_PAGO_JUGADOR_PERIODO UNIQUE (id_jugador, periodo);
GO

-- =========================================================
-- 4. Evitar dos contratos VIGENTE simultaneos para el mismo jugador
--    (indice unico filtrado: solo aplica sobre las filas VIGENTE)
-- =========================================================
CREATE UNIQUE INDEX UX_CONTRATO_JUGADOR_VIGENTE
    ON CONTRATO (id_jugador)
    WHERE estado = 'VIGENTE';
GO

-- =========================================================
-- 5. Indices en columnas de consulta frecuente
-- =========================================================
CREATE INDEX IX_JUGADOR_ESTADO ON JUGADOR (estado);
CREATE INDEX IX_CONTRATO_JUGADOR ON CONTRATO (id_jugador);
CREATE INDEX IX_CONTRATO_ESTADO ON CONTRATO (estado);
CREATE INDEX IX_PARTIDO_FECHA ON PARTIDO (fecha);
CREATE INDEX IX_PARTIDO_ESTADO ON PARTIDO (estado);
CREATE INDEX IX_PARTICIPACION_PARTIDO_ID ON PARTICIPACION_PARTIDO (id_partido);
CREATE INDEX IX_PARTICIPACION_JUGADOR_ID ON PARTICIPACION_PARTIDO (id_jugador);
CREATE INDEX IX_PAGO_PERIODO ON PAGO (periodo);
CREATE INDEX IX_PAGO_ESTADO ON PAGO (estado);
CREATE INDEX IX_INGRESO_FECHA ON INGRESO (fecha);
CREATE INDEX IX_EGRESO_FECHA ON EGRESO (fecha);
GO

-- =========================================================
-- 6. Tabla AUDITORIA
--    (queda lista para que el backend la use en una siguiente etapa;
--    por ahora no la escribe ningun modulo Java)
-- =========================================================
CREATE TABLE AUDITORIA (
    id_auditoria    INT IDENTITY(1,1) PRIMARY KEY,
    id_usuario      INT NULL,
    modulo          NVARCHAR(60)  NOT NULL,
    accion          NVARCHAR(30)  NOT NULL,
    tabla_afectada  NVARCHAR(60)  NULL,
    id_registro     INT NULL,
    descripcion     NVARCHAR(400) NULL,
    fecha_hora      DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_AUDITORIA_USUARIO FOREIGN KEY (id_usuario) REFERENCES USUARIO(id_usuario)
);
CREATE INDEX IX_AUDITORIA_FECHA ON AUDITORIA (fecha_hora);
CREATE INDEX IX_AUDITORIA_USUARIO ON AUDITORIA (id_usuario);
GO

-- =========================================================
-- 7. Tabla TEMPORADA
--    (de referencia por ahora; PARTIDO y PRESUPUESTO se relacionaran
--    con ella cuando el backend se actualice para usarla)
-- =========================================================
CREATE TABLE TEMPORADA (
    id_temporada  INT IDENTITY(1,1) PRIMARY KEY,
    nombre        NVARCHAR(20) NOT NULL UNIQUE,
    fecha_inicio  DATE NOT NULL,
    fecha_fin     DATE NOT NULL,
    estado        NVARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    CONSTRAINT CK_TEMPORADA_FECHAS CHECK (fecha_fin > fecha_inicio),
    CONSTRAINT CK_TEMPORADA_ESTADO CHECK (estado IN ('ACTIVA', 'FINALIZADA'))
);
GO

INSERT INTO TEMPORADA (nombre, fecha_inicio, fecha_fin, estado) VALUES
    (N'2026/2027', '2026-07-01', '2027-06-30', 'ACTIVA');
GO

-- =========================================================
-- 8. Catalogo de POSICION
--    (de referencia; JUGADOR.posicion sigue siendo texto -ya validado
--    por CK_JUGADOR_POSICION arriba- hasta que el backend migre a FK)
-- =========================================================
CREATE TABLE POSICION (
    id_posicion INT IDENTITY(1,1) PRIMARY KEY,
    nombre      NVARCHAR(40) NOT NULL UNIQUE
);
GO

INSERT INTO POSICION (nombre) VALUES
    (N'Portero'), (N'Defensa'), (N'Centrocampista'), (N'Delantero');
GO
