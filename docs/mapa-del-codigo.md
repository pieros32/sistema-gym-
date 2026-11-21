# Mapa del código · IRON LEGACY

**Punto de entrada:** `src/main/java/com/ironlegacy/IronLegacyApplication.java` (`main`).
**Versión documentada:** etapa E6, fecha 2026-11-21. Base del último commit de E5: `41125a1`.

Capas (sección 4 del brief): Presentación (`controlador/` + `templates/`) → Negocio (`servicio/`) → Persistencia (`dao/` y `dao/jdbc/`) → BD (`db/`). El modelo (`modelo/`) lo usan todas. La configuración y el interceptor de roles viven en `config/`.

```
iron-legacy/
├─ pom.xml                        Dependencias y plugin de Spring Boot 3.3.5 (Java 17).
├─ README.md                      Cómo instalar, ejecutar y probar.
├─ .gitignore                     Excluye target/, IDEs y secretos (.env).
├─ db/
│  ├─ schema.sql                  Esquema MySQL 8 (InnoDB). Sin vistas: el estado se calcula en el dominio.
│  └─ seed.sql                    Planes (tarifa provisional 1.00) y usuarios demo con hash BCrypt.
├─ docs/
│  ├─ pruebas.md                  Casos de prueba CP-01 a CP-M20 con resultado obtenido.
│  ├─ mapa-del-codigo.md          Este archivo.
│  └─ capturas/                   Capturas de cada pantalla (01 a 17).
└─ src/
   ├─ main/java/com/ironlegacy/
   │  ├─ IronLegacyApplication.java      Punto de entrada de Spring Boot.
   │  ├─ config/
   │  │  ├─ ConfiguracionFecha.java      Bean Clock en America/Lima: única fuente de la fecha.
   │  │  ├─ ConfiguracionSeguridad.java  Bean BCryptPasswordEncoder.
   │  │  ├─ ClavesSesion.java            Nombres de atributos de HttpSession.
   │  │  ├─ WebConfig.java               Registra el interceptor y excluye login, css y error.
   │  │  ├─ InterceptorRoles.java        Sin sesión → login; sin permiso → 403. Traduce ruta a caso de uso.
   │  │  └─ DatosDemo.java               Socios de demostración (solo perfil demo, solo si no hay socios).
   │  ├─ modelo/                         Registros inmutables con la lógica del dominio.
   │  │  ├─ Rol.java                     Matriz de permisos por rol (RN-08).
   │  │  ├─ Usuario.java                 tienePermiso(caso de uso).
   │  │  ├─ Socio.java                   Datos del socio.
   │  │  ├─ PlanMembresia.java           calcularVencimiento (RN-02).
   │  │  ├─ Membresia.java               esVigente (RN-03) y diasParaVencer.
   │  │  ├─ Pago.java                    Monto mayor que cero (RN-05).
   │  │  ├─ Asistencia.java              esAutorizada.
   │  │  ├─ ResultadoAcceso.java         AUTORIZADO / DENEGADO.
   │  │  ├─ MedioPago.java               EFECTIVO / YAPE / PLIN.
   │  │  ├─ PagoDetalle.java             Fila del historial (CU-07).
   │  │  └─ SocioPorVencer.java          Fila de por vencer (CU-08).
   │  ├─ dao/                            Interfaces, una por entidad. Solo SQL y mapeo.
   │  │  ├─ UsuarioDao.java, SocioDao.java, PlanMembresiaDao.java
   │  │  ├─ MembresiaDao.java            obtenerUltima, crear, buscarPorVencer (RN-07 en SQL), contarSociosActivos.
   │  │  ├─ PagoDao.java                 crear, listarPorSocio, sumarIngresos.
   │  │  └─ AsistenciaDao.java           registrar.
   │  ├─ dao/jdbc/                       Implementaciones con JdbcTemplate y SimpleJdbcInsert.
   │  ├─ servicio/                       Reglas de negocio y transacciones. No conocen HTTP.
   │  │  ├─ SesionServicio.java          CU-01: validación de usuario activo y clave BCrypt.
   │  │  ├─ SocioServicio.java           CU-02 (registrar, @Transactional) y CU-05 (buscar).
   │  │  ├─ MembresiaServicio.java       crear (único punto de membresías), renovar (CU-03, @Transactional),
   │  │  │                               proximosAVencer (CU-08), inicioDeRenovacion (RN-04).
   │  │  ├─ PagoServicio.java            registrar (CU-06) e historialPorDni (CU-07).
   │  │  ├─ AccesoServicio.java          CU-04 con cronómetro en ms en el log.
   │  │  ├─ ReporteServicio.java         CU-09.
   │  │  ├─ NegocioException.java        Error con mensaje para la pantalla.
   │  │  └─ DatosRegistroSocio.java, ResultadoOperacion.java, HistorialPagos.java,
   │  │     VencimientoProximo.java, ResumenReportes.java   Registros de entrada y salida de los servicios.
   │  └─ controlador/                    Recibe la petición, llama al servicio y elige la vista.
   │     ├─ SesionControlador.java      /, /login, /menu, /logout.
   │     ├─ SocioControlador.java       /socios/buscar, /socios/registro.
   │     ├─ MembresiaControlador.java   /membresias/renovar, /membresias/por-vencer.
   │     ├─ AccesoControlador.java      /acceso/validar.
   │     ├─ PagoControlador.java        /pagos/historial.
   │     └─ ReporteControlador.java     /reportes.
   ├─ main/resources/
   │  ├─ application.properties         Conexión y red por variables de entorno (DB_CLAVE obligatoria).
   │  ├─ static/css/estilo.css          Estilos con variables y modo oscuro.
   │  └─ templates/                     Vistas Thymeleaf.
   │     ├─ login.html, menu.html
   │     ├─ acceso/validar.html         CU-04.
   │     ├─ socios/buscar.html, socios/registro.html
   │     ├─ fragmentos/buscar-socio.html  Fragmento reutilizable de CU-05.
   │     ├─ membresias/renovar.html, membresias/por-vencer.html
   │     ├─ pagos/historial.html        CU-07.
   │     └─ reportes/index.html         CU-09.
   └─ test/java/com/ironlegacy/
      ├─ modelo/                        RN-02, RN-03 (unitarias).
      ├─ servicio/                      CU-04 (4 caminos), RN-04, RN-07, validaciones de CU-02 (unitarias con Mockito).
      └─ integracion/                   MySQL real: BaseDePruebas.java (prepara iron_legacy_test),
                                        RegistroRenovacionIntegracionTest (rollback), ConsultasIntegracionTest.
```

## Dónde está cada patrón

- **MVC:** `controlador/` (C), `templates/` (V) y `modelo/` (M).
- **DAO:** interfaces en `dao/`, implementaciones en `dao/jdbc/`. Los servicios dependen de las interfaces.
- **Capas:** cada paquete de `servicio/` solo llama a `dao/` y a `modelo/`. Los controladores solo llaman a `servicio/`.
- **Transacciones:** `SocioServicio.registrar` y `MembresiaServicio.renovar`, anotadas con `@Transactional`.
- **Interceptor de roles:** `config/InterceptorRoles.java`, registrado en `config/WebConfig.java`.

## Decisiones que cambian la estructura del brief

| Cambio | Motivo |
|---|---|
| Sin vista `v_membresia_estado` | El estado se calcula en `Membresia.esVigente` (RN-03). Una sola fuente de verdad. |
| Sin `Socio.actualizarDatos` | Ningún caso de uso la pide (sección 12). |
| Se añadieron `ConfiguracionSeguridad`, `ClavesSesion`, `NegocioException` y los registros de entrada y salida de `servicio/` | Son necesarios para la inyección, la sesión y los mensajes de error. |
| Se añadió `menu.html` | El brief pide "menú según rol" (CU-01). |
| `PagoControlador` solo expone el historial | CU-06 no tiene pantalla. |
| `modelo/` usa `record` en lugar de clases con getters | Inmutables, con lógica propia, y menos código. |
