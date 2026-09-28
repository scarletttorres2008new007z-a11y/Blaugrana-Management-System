/*
    BLAUGRANA MANAGEMENT SYSTEM
    Script 07 - Usuarios de demostracion para cada rol

    Se ejecuta despues de 01-06. Agrega un usuario de ejemplo por cada rol
    distinto de Administrador (que ya se crea en 04_datos_demo.sql), para
    poder probar el nuevo control de permisos por rol.
*/

USE BlaugranaDB;
GO

INSERT INTO USUARIO (nombre_usuario, contrasena_hash, id_rol, nombre_completo, correo, estado)
SELECT v.nombre_usuario, v.contrasena_hash, r.id_rol, v.nombre_completo, v.correo, 'ACTIVO'
FROM ROL r
JOIN (VALUES
    -- usuario: directivo       / contrasena: directivo123
    (N'Directivo',        N'directivo',  '1705a369e59361e7b85a0d3b95c96b82725ff35a874916576ba3281c8d28048d',
     N'Directivo del Club', N'directivo@blaugrana-management.local'),
    -- usuario: deportivo       / contrasena: deportivo123
    (N'Gestor Deportivo',  N'deportivo', '9ea0070fef80621c901559bed7f479e0b6270543474ff4c85ae49afc017b5894',
     N'Gestor Deportivo', N'deportivo@blaugrana-management.local'),
    -- usuario: financiero      / contrasena: financiero123
    (N'Gestor Financiero', N'financiero','3a619fc9d95237480a9a56a0401f3a031cd62aa6989a8c5bce9a23159b088cda',
     N'Gestor Financiero', N'financiero@blaugrana-management.local')
) AS v(nombre_rol, nombre_usuario, contrasena_hash, nombre_completo, correo)
    ON r.nombre_rol = v.nombre_rol;
GO
