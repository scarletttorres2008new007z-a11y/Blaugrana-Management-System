/*
    BLAUGRANA MANAGEMENT SYSTEM
    Script 01 - Creacion de la base de datos
    Motor: Microsoft SQL Server
*/

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'BlaugranaDB')
BEGIN
    CREATE DATABASE BlaugranaDB;
END
GO

USE BlaugranaDB;
GO
