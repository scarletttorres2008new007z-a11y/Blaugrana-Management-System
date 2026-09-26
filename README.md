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
│   └── 05_consultas_reportes.sql
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

### Usuario de demostración

| Usuario | Contraseña |
|---------|------------|
| admin   | admin123   |

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

## Aviso

Este proyecto se entrega con fines exclusivamente académicos. Los nombres
de jugadores y del cuerpo técnico corresponden a datos públicos usados
únicamente como ejemplo de carga de datos; los valores monetarios son
ficticios y no reflejan cifras reales del club.
