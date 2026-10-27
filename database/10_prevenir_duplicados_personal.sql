/*
    BLAUGRANA MANAGEMENT SYSTEM
    Script 10 - Evitar personal duplicado
    Motor: Microsoft SQL Server

    PERSONAL, igual que paso con PARTIDO (ver script 08), no tenia ninguna
    restriccion que impidiera insertar el mismo miembro del cuerpo tecnico
    mas de una vez si el script 04_datos_demo.sql se ejecutaba por error
    mas de una vez.

    Este script agrega un indice UNICO filtrado sobre el documento (solo
    para las filas donde el documento no es nulo ni vacio, ya que el campo
    es opcional). No modifica ninguna tabla en su forma ni requiere cambios
    en la aplicacion Java. Ejecutar DESPUES de limpiar los duplicados
    actuales (ver instrucciones aparte).
*/

USE BlaugranaDB;
GO

CREATE UNIQUE INDEX UX_PERSONAL_DOCUMENTO
    ON PERSONAL (documento)
    WHERE documento IS NOT NULL AND documento <> '';
GO
