/*
    BLAUGRANA MANAGEMENT SYSTEM
    Script 02 - Creacion de tablas (sin llaves foraneas)
    Motor: Microsoft SQL Server
*/

USE BlaugranaDB;
GO

-- =========================================================
-- MODULO: SEGURIDAD / ADMINISTRACION
-- =========================================================

CREATE TABLE ROL (
    id_rol          INT IDENTITY(1,1) PRIMARY KEY,
    nombre_rol      NVARCHAR(50)  NOT NULL UNIQUE,
    descripcion     NVARCHAR(200) NULL
);
GO

CREATE TABLE USUARIO (
    id_usuario       INT IDENTITY(1,1) PRIMARY KEY,
    nombre_usuario   NVARCHAR(50)  NOT NULL UNIQUE,
    contrasena_hash  NVARCHAR(256) NOT NULL,
    id_rol           INT NOT NULL,
    nombre_completo  NVARCHAR(150) NOT NULL,
    correo           NVARCHAR(150) NULL,
    estado           NVARCHAR(20)  NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion   DATE NOT NULL DEFAULT CAST(GETDATE() AS DATE)
);
GO

-- =========================================================
-- MODULO: CLUB
-- =========================================================

CREATE TABLE JUGADOR (
    id_jugador       INT IDENTITY(1,1) PRIMARY KEY,
    nombre           NVARCHAR(80) NOT NULL,
    apellido         NVARCHAR(80) NOT NULL,
    documento        NVARCHAR(30) NULL,
    fecha_nacimiento DATE NULL,
    nacionalidad     NVARCHAR(60) NULL,
    posicion         NVARCHAR(40) NOT NULL,
    numero_camiseta  INT NOT NULL,
    fecha_ingreso    DATE NOT NULL DEFAULT CAST(GETDATE() AS DATE),
    estado           NVARCHAR(20) NOT NULL DEFAULT 'ACTIVO'
);
GO

CREATE TABLE PERSONAL (
    id_personal      INT IDENTITY(1,1) PRIMARY KEY,
    nombre           NVARCHAR(80) NOT NULL,
    apellido         NVARCHAR(80) NOT NULL,
    documento        NVARCHAR(30) NULL,
    cargo            NVARCHAR(80) NOT NULL,
    area             NVARCHAR(60) NULL,
    fecha_nacimiento DATE NULL,
    nacionalidad     NVARCHAR(60) NULL,
    fecha_ingreso    DATE NOT NULL DEFAULT CAST(GETDATE() AS DATE),
    estado           NVARCHAR(20) NOT NULL DEFAULT 'ACTIVO'
);
GO

CREATE TABLE CONTRATO (
    id_contrato   INT IDENTITY(1,1) PRIMARY KEY,
    id_jugador    INT NOT NULL,
    fecha_inicio  DATE NOT NULL,
    fecha_fin     DATE NOT NULL,
    salario_base  DECIMAL(12,2) NOT NULL,
    condiciones   NVARCHAR(400) NULL,
    estado        NVARCHAR(20) NOT NULL DEFAULT 'VIGENTE'
);
GO

-- =========================================================
-- MODULO: DEPORTIVO
-- =========================================================

CREATE TABLE PARTIDO (
    id_partido    INT IDENTITY(1,1) PRIMARY KEY,
    competicion   NVARCHAR(60) NOT NULL,
    fecha         DATE NOT NULL,
    rival         NVARCHAR(80) NOT NULL,
    condicion     NVARCHAR(20) NOT NULL,           -- LOCAL / VISITANTE
    goles_favor   INT NOT NULL DEFAULT 0,
    goles_contra  INT NOT NULL DEFAULT 0,
    resultado     NVARCHAR(20) NULL,               -- GANADO / PERDIDO / EMPATADO
    estado        NVARCHAR(20) NOT NULL DEFAULT 'PROGRAMADO'
);
GO

CREATE TABLE PARTICIPACION_PARTIDO (
    id_participacion   INT IDENTITY(1,1) PRIMARY KEY,
    id_partido         INT NOT NULL,
    id_jugador         INT NOT NULL,
    minutos_jugados    INT NOT NULL DEFAULT 0,
    goles              INT NOT NULL DEFAULT 0,
    asistencias        INT NOT NULL DEFAULT 0,
    tarjetas_amarillas INT NOT NULL DEFAULT 0,
    tarjetas_rojas     INT NOT NULL DEFAULT 0,
    titular            BIT NOT NULL DEFAULT 0
);
GO

CREATE TABLE BONIFICACION (
    id_bonificacion   INT IDENTITY(1,1) PRIMARY KEY,
    id_jugador        INT NOT NULL,
    id_partido        INT NULL,
    concepto          NVARCHAR(100) NOT NULL,
    valor             DECIMAL(12,2) NOT NULL,
    fecha_generacion  DATE NOT NULL DEFAULT CAST(GETDATE() AS DATE)
);
GO

-- =========================================================
-- MODULO: FINANZAS
-- =========================================================

CREATE TABLE PAGO (
    id_pago         INT IDENTITY(1,1) PRIMARY KEY,
    id_jugador      INT NOT NULL,
    id_contrato     INT NOT NULL,
    periodo         NVARCHAR(20) NOT NULL,   -- ej. '2026-09'
    salario_base    DECIMAL(12,2) NOT NULL,
    bonificaciones  DECIMAL(12,2) NOT NULL DEFAULT 0,
    deducciones     DECIMAL(12,2) NOT NULL DEFAULT 0,
    total           DECIMAL(12,2) NOT NULL,
    fecha_pago      DATE NULL,
    estado          NVARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'
);
GO

CREATE TABLE CATEGORIA_INGRESO (
    id_categoria_ingreso INT IDENTITY(1,1) PRIMARY KEY,
    nombre               NVARCHAR(80) NOT NULL UNIQUE
);
GO

CREATE TABLE INGRESO (
    id_ingreso            INT IDENTITY(1,1) PRIMARY KEY,
    id_categoria_ingreso  INT NOT NULL,
    descripcion           NVARCHAR(200) NULL,
    monto                 DECIMAL(12,2) NOT NULL,
    fecha                 DATE NOT NULL
);
GO

CREATE TABLE CATEGORIA_EGRESO (
    id_categoria_egreso INT IDENTITY(1,1) PRIMARY KEY,
    nombre               NVARCHAR(80) NOT NULL UNIQUE
);
GO

CREATE TABLE EGRESO (
    id_egreso            INT IDENTITY(1,1) PRIMARY KEY,
    id_categoria_egreso  INT NOT NULL,
    descripcion          NVARCHAR(200) NULL,
    monto                DECIMAL(12,2) NOT NULL,
    fecha                DATE NOT NULL
);
GO

CREATE TABLE PRESUPUESTO (
    id_presupuesto  INT IDENTITY(1,1) PRIMARY KEY,
    temporada       NVARCHAR(20) NOT NULL,
    nombre          NVARCHAR(100) NOT NULL,
    fecha_creacion  DATE NOT NULL DEFAULT CAST(GETDATE() AS DATE)
);
GO

CREATE TABLE DETALLE_PRESUPUESTO (
    id_detalle           INT IDENTITY(1,1) PRIMARY KEY,
    id_presupuesto       INT NOT NULL,
    categoria            NVARCHAR(80) NOT NULL,
    monto_presupuestado  DECIMAL(12,2) NOT NULL DEFAULT 0,
    monto_ejecutado      DECIMAL(12,2) NOT NULL DEFAULT 0
);
GO
