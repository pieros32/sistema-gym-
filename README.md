# IRON LEGACY · Control de membresías

Sistema web para la recepción de un gimnasio: registro de socios, renovación de membresías, pagos, validación de acceso diario, historial de pagos, alertas de vencimiento y reportes.

Trabajo del curso Análisis y Diseño de Sistemas (UTP). Arquitectura en capas con MVC, DAO y JDBC sobre MySQL 8.

## Requisitos

- Java 17 o superior (probado con Java 21)
- Maven 3.9 o superior, **o** el Maven Wrapper incluido (`mvnw` / `mvnw.cmd`), que descarga Maven solo y no requiere instalarlo
- MySQL 8 (probado con 8.0.46)

## 1. Preparar la base de datos

Como administrador de MySQL, crea el usuario de desarrollo y las bases. 

```bash
sudo mysql -e "CREATE USER IF NOT EXISTS 'iron_legacy'@'localhost' IDENTIFIED BY '<clave-de-desarrollo>';"
sudo mysql -e "GRANT ALL ON iron_legacy.* TO 'iron_legacy'@'localhost';"
sudo mysql -e "GRANT ALL ON iron_legacy_test.* TO 'iron_legacy'@'localhost';"
sudo mysql < db/schema.sql
sudo mysql < db/seed.sql
```

`GRANT ... iron_legacy_test` solo hace falta para las pruebas de integración, que crean esa base automáticamente.

## 2. Ejecutar

```bash
DB_CLAVE='<clave-de-desarrollo>' mvn spring-boot:run
```

Abre `http://localhost:8080`. Desde otra PC de la red, usa la IP de la PC de recepción y el mismo puerto.

Variables de entorno (solo `DB_CLAVE` es obligatoria):

| Variable | Valor por defecto | Para qué sirve |
|---|---|---|
| `DB_CLAVE` | (obligatoria, sin valor por defecto) | Clave del usuario MySQL. La aplicación no arranca sin ella. |
| `DB_USUARIO` | `iron_legacy` | Usuario MySQL |
| `DB_URL` | `jdbc:mysql://localhost:3306/iron_legacy?serverTimezone=America/Lima&useSSL=false&allowPublicKeyRetrieval=true` | Cadena de conexión |
| `PUERTO` | `8080` | Puerto HTTP |
| `SERVIDOR_DIRECCION` | `0.0.0.0` | Interfaz de escucha. `0.0.0.0` permite el acceso desde la red local (RNF-06). |

### Sin instalar Maven (Windows)

Usa el wrapper en lugar de `mvn`. Solo necesitas Java 17 o superior:

```bat
set DB_CLAVE=<clave-de-desarrollo>
mvnw.cmd spring-boot:run
```

En Linux o macOS: `./mvnw spring-boot:run`.

### Con datos de demostración

El perfil `demo` crea 8 socios con membresías vigentes, por vencer y vencidas, y sus pagos. Las fechas se calculan a partir de hoy, así que la demostración no caduca. Solo se crean si la tabla `socio` está vacía:

```bash
DB_CLAVE='<clave-de-desarrollo>' mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

### Usuarios de demostración

| Usuario | Clave | Rol |
|---|---|---|
| `admin` | `Admin#2026` | Administrador |
| `recepcion` | `Recep#2026` | Recepcionista |

Son solo para la demostración local. Cámbialas antes de usar el sistema con datos reales. Están en este README y nunca en el código; la base guarda solo el hash BCrypt.

## 3. Pruebas

```bash
mvn test                                    # unitarias: no necesitan MySQL
DB_CLAVE='<clave-de-desarrollo>' mvn test   # incluye las pruebas de integración contra MySQL
```

- **Unitarias (21):** reglas RN-02, RN-03, RN-04 y RN-07, los cuatro caminos de CU-04, y las validaciones de CU-02. Usan Mockito y no tocan la base.
- **Integración (11):** corren contra MySQL real en `iron_legacy_test`, creada desde `db/schema.sql` y `db/seed.sql` (solo cambia el nombre). Cubren el rollback de CU-02 y CU-03 cuando falla el pago, la regla de por vencer en SQL y los reportes. Sin `DB_CLAVE` esa clase se omite.



## 4. Uso por rol

| Pantalla | Recepcionista | Administrador |
|---|---|---|
| Validar acceso (CU-04) | Sí | No |
| Registrar socio nuevo (CU-02) | Sí | No |
| Renovar membresía (CU-03) | Sí | No |
| Buscar socio (CU-05) | Sí | Sí |
| Historial de pagos (CU-07) | Sí | Sí |
| Por vencer (CU-08) | Sí | Sí |
| Reportes (CU-09) | No (403) | Sí |

Sin sesión, cualquier página redirige a `/login`.

## 5. Tarifas

Los planes de `db/seed.sql` tienen la tarifa vigente, en soles:

| Plan | Duración | Precio |
|---|---|---|
| Mensual | 1 mes | 60.00 |
| Trimestral | 3 meses | 150.00 |

Para cambiarlas en una base que ya existe (el sistema no tiene pantalla para esto):

```sql
UPDATE plan_membresia SET precio = 60.00  WHERE nombre = 'Mensual';
UPDATE plan_membresia SET precio = 150.00 WHERE nombre = 'Trimestral';
```

El cambio solo afecta a los pagos nuevos; los pagos ya registrados conservan su monto.

## 6. Decisiones y limitaciones

- **Regla de vencimiento (RN-02):** vencimiento = inicio + duración − 1 día. Con inicio el 31 de enero, un plan Mensual vence el 27 de febrero (28 días contando ambos extremos). Se mantuvo la regla tal cual y queda como caso borde en las pruebas. **Pendiente de confirmar con el administrador.**
- **Fecha y zona horaria:** todas las fechas salen de un `Clock` en `America/Lima`. La base no usa `CURDATE()` ni `NOW()`.
- **Tiempo de validación (RNF-02):** la aplicación registra en el log los milisegundos de `validarAcceso`. En 100 validaciones de prueba, el promedio fue de 3.15 ms. Es un dato medido, no garantizado, y debe validarse con el administrador.
- **Acceso desde la red:** el servidor escucha en `0.0.0.0` y sirve HTTP sin cifrar. En una red local de confianza basta; si el sistema sale de esa red, debe ir detrás de HTTPS.
- **Datos de demostración:** `DatosDemo` no es transaccional. Si falla a mitad, la tabla queda incompleta y hay que reiniciar la base.
- **Duplicado de DNI en simultáneo:** la restricción `UNIQUE` de la base es la última barrera. El código traduce el error al mensaje correcto, aunque no se probó con dos altas reales a la vez.
- **Desviaciones del diseño original:** sin vista `v_membresia_estado` (el estado se calcula en el dominio) y sin edición de datos del socio (ningún caso de uso la pide).

