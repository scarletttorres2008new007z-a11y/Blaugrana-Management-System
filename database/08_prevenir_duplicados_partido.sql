/*
    BLAUGRANA MANAGEMENT SYSTEM
    Script 08 - Evitar partidos duplicados
    Motor: Microsoft SQL Server

    PARTIDO quedo fuera de las restricciones UNIQUE que agrego el script 06
    (que si protege a JUGADOR, PARTICIPACION_PARTIDO y PAGO). Por eso,
    volver a ejecutar el bloque de PARTIDO de 04_datos_demo.sql mas de una
    vez genero partidos repetidos (misma competicion/fecha/rival/condicion)
    sin que SQL Server lo impidiera.

    Este script:
      1. Agrega una restriccion UNIQUE para que esto no pueda volver a pasar.

    No modifica ninguna tabla existente en su forma ni requiere cambios en
    la aplicacion Java. Ejecutar DESPUES de limpiar los duplicados actuales
    (ver instrucciones aparte).
*/

USE BlaugranaDB;
GO

ALTER TABLE PARTIDO
    ADD CONSTRAINT UQ_PARTIDO_UNICO UNIQUE (competicion, fecha, rival, condicion);
GO
