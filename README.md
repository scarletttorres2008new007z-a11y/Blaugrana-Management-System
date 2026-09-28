# Blaugrana Management System

**Sistema Integral de Gestión Deportiva, Financiera y Administrativa**

Proyecto académico de escritorio (Java + Swing + JDBC + Microsoft SQL Server)
que simula el sistema interno de gestión de un club de fútbol profesional.
La estética, los nombres de jugadores y del cuerpo técnico y los datos de
demostración están **inspirados en el FC Barcelona**, pero se trata de un
software ficticio hecho con fines académicos: no es una aplicación oficial
del club, no está afiliado a él y los montos económicos (salarios, ingresos,
egresos y presupuesto) son cifras inventadas para ilustrar el funcionamiento
del sistema.

## Flujo del sistema

```
Jugador -> Contrato -> Partido -> Rendimiento -> Bonificación -> Pago -> Egreso -> Balance -> Reporte
```

Los módulos deportivos (plantilla, partidos, rendimiento) alimentan los
módulos financieros (bonificaciones, pagos, presupuesto), de modo que una
decisión deportiva tiene una consecuencia económica visible en los reportes.

## Tecnologías

- Java 17+ (Swing para la interfaz de escritorio)
- JDBC con el driver oficial `mssql-jdbc`
- Microsoft SQL Server
- Maven para la gestión de dependencias y el empaquetado

## Estructura del proyecto

```
Blaugrana-Management-System/
├── pom.xml
├── database/
│   ├── 01_crear_base_datos.sql
│   ├── 02_crear_tablas.sql
│   ├── 03_crear_relaciones.sql
│   ├── 04_datos_demo.sql
│   ├── 05_consultas_reportes.sql
│   ├── 06_mejoras_integridad.sql
│   └── 07_usuarios_demo_roles.sql
└── src/main/java/sv/udb/blaugrana/
    ├── Main.java
    ├── config/        (conexión JDBC)
    ├── model/         (entidades del dominio)
    ├── dao/           (acceso a datos con JDBC)
    ├── service/       (reglas de negocio)
    ├── session/       (sesión del usuario autenticado)
    ├── util/          (formato, validaciones, mensajes, colores)
    └── view/          (pantallas Swing, una carpeta por módulo)
```

## Puesta en marcha

### 1. Base de datos

Ejecute los scripts de `database/` en orden contra una instancia de
Microsoft SQL Server:

1. `01_crear_base_datos.sql` – crea la base de datos `BlaugranaDB`.
2. `02_crear_tablas.sql` – crea las tablas del sistema.
3. `03_crear_relaciones.sql` – agrega las llaves foráneas.
4. `04_datos_demo.sql` – carga los datos de demostración (plantilla,
   cuerpo técnico, contratos, partidos, bonificaciones, pagos, ingresos,
   egresos y presupuesto).
5. `05_consultas_reportes.sql` – consultas de referencia usadas por el
   módulo de reportes (opcional, solo para validación manual).
6. `06_mejoras_integridad.sql` – agrega validaciones `CHECK`, restricciones
   `UNIQUE` (evita participaciones y pagos duplicados), un índice que impide
   dos contratos `VIGENTE` simultáneos para un mismo jugador, índices de
   rendimiento y las tablas de referencia `AUDITORIA`, `TEMPORADA` y
   `POSICION`. No requiere cambios en la aplicación Java; se puede ejecutar
   sobre una base de datos que ya tenga cargados los datos de demostración.
7. `07_usuarios_demo_roles.sql` – crea un usuario de demostración para cada
   rol distinto de Administrador (Directivo, Gestor Deportivo, Gestor
   Financiero), útil para probar el control de permisos por rol.

### 2. Configuración de conexión

Edite `src/main/resources/config.properties` con los datos de su instancia:

```properties
db.host=localhost
db.port=1433
db.nombre=BlaugranaDB
db.usuario=sa
db.contrasena=CambiarEstaClave123
db.encrypt=false
```

### 3. Compilar y ejecutar

```bash
mvn package
java -jar target/blaugrana-management-system.jar
```

### Usuarios de demostración

| Usuario     | Contraseña      | Rol                |
|-------------|-----------------|--------------------|
| admin       | admin123        | Administrador      |
| directivo   | directivo123    | Directivo          |
| deportivo   | deportivo123    | Gestor Deportivo   |
| financiero  | financiero123   | Gestor Financiero  |

Los tres últimos se crean con `07_usuarios_demo_roles.sql`.

## Reglas de negocio destacadas

- **Bonificaciones**: al finalizar un partido, el módulo *Partidos* permite
  generar automáticamente las bonificaciones de cada jugador participante
  según su rendimiento: **$500** por gol, **$250** por asistencia y
  **$1,000** por victoria del equipo (para los titulares).
- **Pagos**: el módulo *Pagos* calcula el pago mensual de un jugador a
  partir de su contrato vigente (salario anual / 12) más las bonificaciones
  generadas en el periodo, menos las deducciones indicadas.
- **Finanzas**: el balance general se calcula como la diferencia entre el
  total de ingresos y el total de egresos registrados.

## Permisos por rol

El menú lateral y los módulos accesibles dependen del rol del usuario
autenticado (`sv.udb.blaugrana.session.Permisos`):

| Módulo | Administrador | Directivo | Gestor Deportivo | Gestor Financiero |
|---|:---:|:---:|:---:|:---:|
| Dashboard | ✔ | ✔ | ✔ | ✔ |
| Jugadores / Personal | ✔ | ✔ | Jugadores | — |
| Contratos | ✔ | ✔ | — | ✔ |
| Partidos / Rendimiento | ✔ | ✔ | ✔ | — |
| Bonificaciones | ✔ | ✔ | ✔ | ✔ |
| Pagos / Ingresos / Egresos / Presupuesto | ✔ | ✔ | — | ✔ |
| Reportes | ✔ | ✔ | ✔ | ✔ |
| Usuarios | ✔ | — | — | — |

El rol **Directivo** ve los mismos módulos que el Administrador (salvo
Usuarios) pero en modo de **solo consulta**: los botones que crean,
modifican o eliminan información quedan deshabilitados.

## Reglas de negocio y validaciones del backend

- **Evita duplicados antes de tocar la base de datos**: no se puede generar
  dos veces el pago de un jugador para el mismo periodo, ni registrar dos
  veces la participación de un jugador en el mismo partido, ni crear un
  segundo contrato `VIGENTE` para un jugador que ya tiene uno, ni repetir un
  número de camiseta. Estas mismas reglas están respaldadas por las
  restricciones `UNIQUE`/`CHECK` de `06_mejoras_integridad.sql` como última
  línea de defensa.
- **Transacciones**: generar las bonificaciones de un partido (varias filas)
  y marcar un pago como pagado (que además genera su egreso) se ejecutan
  como una sola transacción JDBC — si algo falla a mitad de camino, no queda
  ningún registro parcial.
- **Pago → Egreso automático**: al marcar un pago como `PAGADO`, el sistema
  registra automáticamente un egreso en la categoría "Salarios" por el
  mismo monto, manteniendo la situación financiera del club sincronizada
  sin pasos manuales adicionales.
- **Mensajes de error amigables**: las violaciones de `CHECK`, `UNIQUE` o
  llaves foráneas que SQL Server rechaza se traducen a mensajes en español
  entendibles (`sv.udb.blaugrana.util.ErroresBD`) en lugar de mostrar la
  excepción técnica cruda.
- **Búsqueda en tablas**: los listados principales (jugadores, personal,
  contratos, partidos, pagos, ingresos, egresos, bonificaciones, usuarios)
  tienen un campo de búsqueda que filtra las filas en tiempo real, sin
  distinguir mayúsculas/minúsculas ni acentos.

## Integridad de datos (`06_mejoras_integridad.sql`)

- Un jugador no puede tener dos contratos `VIGENTE` al mismo tiempo, ni dos
  registros de participación para el mismo partido, ni dos pagos para el
  mismo periodo — la base de datos los rechaza aunque la aplicación lo
  permitiera.
- El número de camiseta es único y debe estar entre 1 y 99; los montos de
  salarios, ingresos, egresos y bonificaciones deben ser positivos, y el
  total de un pago debe coincidir exactamente con salario + bonificaciones −
  deducciones.
- Se agregan las tablas `AUDITORIA`, `TEMPORADA` y `POSICION` como base
  para una futura integración desde el backend (por ahora no las escribe
  ningún módulo Java).

## Aviso

Este proyecto se entrega con fines exclusivamente académicos. Los nombres
de jugadores y del cuerpo técnico corresponden a datos públicos usados
únicamente como ejemplo de carga de datos; los valores monetarios son
ficticios y no reflejan cifras reales del club.
