# IRON LEGACY · Control de membresías

Sistema web para la recepción de un gimnasio: registro de socios, renovación de membresías, pagos, validación de acceso diario, historial, alertas de vencimiento y reportes.

> Estado: en desarrollo. Esta versión cubre las etapas E1 a E5: conexión a MySQL, login con roles, búsqueda de socios, validación de acceso, alta y renovación con transacciones, historial de pagos, membresías por vencer y reportes. El resto se documenta en las etapas siguientes.

## Requisitos

- Java 17 o superior (probado con Java 21)
- Maven 3.9+
- MySQL 8

## Preparar la base de datos

Como administrador de MySQL:

```bash
sudo mysql -e "CREATE USER IF NOT EXISTS 'iron_legacy'@'localhost' IDENTIFIED BY '<clave-de-desarrollo>';"
sudo mysql -e "GRANT ALL ON iron_legacy.* TO 'iron_legacy'@'localhost';"
sudo mysql < db/schema.sql
sudo mysql < db/seed.sql
```

Reemplaza `<clave-de-desarrollo>` por una clave propia. No la guardes en el repositorio.

## Ejecutar

```bash
DB_CLAVE='<clave-de-desarrollo>' mvn spring-boot:run
```

Variables de entorno (todas opcionales salvo `DB_CLAVE`):

| Variable | Valor por defecto | Para qué sirve |
|---|---|---|
| `DB_CLAVE` | (obligatoria) | Clave del usuario MySQL |
| `DB_USUARIO` | `iron_legacy` | Usuario MySQL |
| `DB_URL` | `jdbc:mysql://localhost:3306/iron_legacy?serverTimezone=America/Lima&useSSL=false&allowPublicKeyRetrieval=true` | Cadena de conexión |
| `PUERTO` | `8080` | Puerto HTTP |
| `SERVIDOR_DIRECCION` | `0.0.0.0` | Interfaz de escucha; `0.0.0.0` permite el acceso desde otras PCs de la red |

Abre `http://localhost:8080` (o la IP de la PC de recepción desde otra máquina).

### Con datos de demostración

El perfil `demo` crea 8 socios con membresías vigentes, por vencer y vencidas. Las fechas son relativas a hoy. Solo se crean si la tabla `socio` está vacía:

```bash
DB_CLAVE='<clave-de-desarrollo>' mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

## Pruebas

```bash
mvn test                                  # unitarias: no necesitan MySQL
DB_CLAVE='<clave-de-desarrollo>' mvn test  # incluye la integración contra MySQL (crea iron_legacy_test)
```

La integración crea `iron_legacy_test` a partir de `db/schema.sql` y `db/seed.sql`, y la prueba de rollback de CU-02 y CU-03 corre contra MySQL real. Sin `DB_CLAVE` esa clase se omite.

## Usuarios de demostración

| Usuario | Clave | Rol |
|---|---|---|
| `admin` | `Admin#2026` | Administrador |
| `recepcion` | `Recep#2026` | Recepcionista |

Estas credenciales son solo para la demostración local. Cámbialas antes de usar el sistema con datos reales.

## Tarifas

Los precios de `db/seed.sql` (Mensual y Trimestral) son **provisionales, de 1.00**, marcados con `-- TARIFA PENDIENTE`. La tarifa real la entrega el administrador.

## Estructura

Ver la sección 4 del brief y, al cerrar el proyecto, `docs/mapa-del-codigo.md`.
