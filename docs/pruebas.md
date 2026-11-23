# Casos de prueba · IRON LEGACY

Formato de validación: ID, caso de uso, entrada, resultado esperado, resultado obtenido y estado.
Todas las pruebas automáticas se ejecutaron con `DB_CLAVE=<clave> mvn test` (MySQL 8.0.46, Java 21).

**Resumen:** 32 pruebas automáticas, 32 aprobadas, 0 fallidas, 0 omitidas. Pruebas manuales por HTTP: 15, todas con el resultado esperado.

## A. Pruebas automáticas

### A1. Reglas de dominio (unitarias)

| ID | CU / RN | Prueba | Entrada | Resultado esperado | Obtenido | Estado |
|---|---|---|---|---|---|---|
| CP-01 | RN-02 | `mensualVenceUnDiaAntesDeUnMesDesdeElInicio` | Mensual, inicio 01-ene-2026 | Vence 31-ene-2026 | 31-ene-2026 | Aprobada |
| CP-02 | RN-02 | `trimestralVenceUnDiaAntesDeTresMeses` | Trimestral, inicio 01-oct-2026 | Vence 31-dic-2026 | 31-dic-2026 | Aprobada |
| CP-03 | RN-02 (caso borde) | `finDeMesAcortaElVencimientoAlMismoDiaDelMesSiguienteMenosUno` | Mensual, inicio 31-ene-2026 | Vence 27-feb-2026 (regla tal cual, ver README) | 27-feb-2026 | Aprobada |
| CP-04 | RN-03 | `vigenteElMismoDiaDelVencimiento` | Vence hoy | Vigente, 0 días restantes | Vigente, 0 días | Aprobada |
| CP-05 | RN-03 | `vencidaAlDiaSiguienteAlVencimiento` | Vence ayer | No vigente | No vigente | Aprobada |
| CP-06 | RN-03 | `diasParaVencerCuentaHastaElVencimiento` | Vence en 5 días | 5 días restantes | 5 | Aprobada |
| CP-07 | Modelo | `rechazaUnVencimientoNoPosteriorAlInicio` | Vencimiento = inicio | Excepción | `IllegalArgumentException` | Aprobada |

### A2. Reglas de negocio de servicio (unitarias, DAO simulados)

| ID | CU / RN | Prueba | Entrada | Resultado esperado | Obtenido | Estado |
|---|---|---|---|---|---|---|
| CP-08 | CU-03 / RN-04 | `renovarVigenteEmpiezaElDiaSiguienteAlVencimiento` | Vence en 10 días | Inicio = vencimiento + 1 | Igual | Aprobada |
| CP-09 | CU-03 / RN-04 | `renovarQueVenceHoyEmpiezaMananaPorqueSigueVigenteHoy` | Vence hoy | Inicio = mañana | Igual | Aprobada |
| CP-10 | CU-03 / RN-04 | `renovarVencidaEmpiezaHoy` | Venció hace 10 días | Inicio = hoy | Igual | Aprobada |
| CP-11 | CU-03 / RN-04 | `socioSinMembresiaEmpiezaHoy` | Sin membresías | Inicio = hoy | Igual | Aprobada |
| CP-12 | CU-08 / RN-07 | `consultaLaVentanaDeSieteDiasYCalculaLosDiasRestantes` | Membresía que vence en 3 días | Consulta hoy..hoy+7; 3 días restantes | Igual | Aprobada |
| CP-13 | CU-02 | `rechazaUnDniDeSieteDigitos` | DNI "1234567" | Mensaje "El DNI debe tener 8 dígitos." y no se crea socio | Igual | Aprobada |
| CP-14 | CU-02 | `rechazaUnTelefonoQueNoTieneNueveDigitos` | Teléfono "98765" | Mensaje de 9 dígitos | Igual | Aprobada |
| CP-15 | CU-02 / RN-01 | `rechazaUnDniQueYaExisteSinCrearNada` | DNI existente | Mensaje de DNI repetido; no se crea socio ni pago | Igual | Aprobada |

### A3. CU-04 Validar acceso (los cuatro caminos más el aviso)

| ID | CU | Prueba | Entrada | Resultado esperado | Obtenido | Estado |
|---|---|---|---|---|---|---|
| CP-16 | CU-04 camino 1 | `autorizaSiLaMembresiaEstaVigenteYRegistraAsistencia` | Membresía vigente | AUTORIZADO, asistencia registrada sin motivo | Igual | Aprobada |
| CP-17 | CU-04 camino 2 | `deniegaConMotivoMembresiaVencidaYRegistraAsistencia` | Membresía vencida | DENEGADO "Membresía vencida", asistencia registrada | Igual | Aprobada |
| CP-18 | CU-04 camino 3 | `deniegaConMotivoSinMembresiaYRegistraAsistencia` | Socio sin membresías | DENEGADO "Sin membresía", asistencia registrada | Igual | Aprobada |
| CP-19 | CU-04 camino 4 | `socioNoExisteNoRegistraNada` | DNI inexistente | "Socio no encontrado.", ninguna asistencia | Igual | Aprobada |
| CP-20 | CU-04 aviso | `avisaCuandoLaMembresiaVenceEnSieteDiasOMenos` | Vence en 7 días | AUTORIZADO con aviso de 7 días | Igual | Aprobada |
| CP-21 | CU-04 / RNF-04 | `rechazaUnDniQueNoTieneOchoDigitosSinConsultarLaBase` | DNI "1234" | Error de validación, sin consulta a BD | Igual | Aprobada |

### A4. Pruebas de integración contra MySQL real

Base de prueba `iron_legacy_test`, creada desde `db/schema.sql` y `db/seed.sql` (solo cambia el nombre).

| ID | CU / RN | Prueba | Entrada | Resultado esperado | Obtenido | Estado |
|---|---|---|---|---|---|---|
| CP-22 | CU-02 / CU-06 | `registrarCreaSocioMembresiaYPagoConLasFechasDelPlan` | Alta válida | 1 socio, 1 membresía (hoy a hoy+1 mes−1), 1 pago | Igual | Aprobada |
| CP-23 | CU-02 **rollback** | `siFallaElPagoElAltaNoDejaNingunRegistro` | Pago forzado a fallar (SpyBean) | 0 socios, 0 membresías, 0 pagos | Igual | Aprobada |
| CP-24 | CU-03 **rollback** | `siFallaElPagoLaRenovacionNoDejaMembresiaNiPagoNuevos` | Renovación con pago forzado a fallar | Sigue 1 membresía y 1 pago | Igual | Aprobada |
| CP-25 | CU-03 / RN-04 | `renovarUnaMembresiaVigenteEmpiezaElDiaSiguienteAlVencimiento` | Renovar socio vigente | Nueva membresía inicia el día siguiente | Igual | Aprobada |
| CP-26 | CU-02 / RN-01 | `noPermiteDosAltasConElMismoDni` | Mismo DNI dos veces | Segundo alta rechazada, 1 socio | Igual | Aprobada |
| CP-27 | CU-03 | `noRenuevaUnDniQueNoExiste` | DNI inexistente | "No existe un socio con ese DNI." | Igual | Aprobada |
| CP-28 | CU-07 | `historialDelSocioVaDelPagoMasRecienteAlMasAntiguo` | Alta y renovación | Primer pago = el más reciente, con periodo y usuario | Igual | Aprobada |
| CP-29 | CU-07 | `historialRechazaUnDniQueNoExiste` | DNI inexistente | Mensaje de socio no existe | Igual | Aprobada |
| CP-30 | CU-08 / RN-07 | `porVencerIncluyeLasVentanaDeSieteDiasYExcluyeLosSociosQueYaRenovaron` | Socios con vencimientos en distintas fechas y uno con membresía posterior | Solo los socios con vencimiento entre hoy y hoy+7 sin membresía posterior, por fecha | Igual | Aprobada |
| CP-31 | CU-09 | `reportesCuentanSociosActivosIngresosDelMesYPorVencer` | 2 altas hoy | 2 socios activos, 2.00 de ingresos del mes, 0 por vencer; mes anterior 0.00 | Igual | Aprobada |
| CP-32 | CU-09 | `reportesRechazanUnMesMalFormado` | Mes "2026-13" | "El mes debe tener el formato AAAA-MM." | Igual | Aprobada |

## B. Pruebas manuales (HTTP contra la aplicación en ejecución)

Datos: base de demostración (`-Dspring-boot.run.profiles=demo`), salvo CP-M08 a CP-M14, que se verificaron con los socios cargados a mano en E3 y E4 (Ana, Luis, Marta, con fechas de vencimiento de esa carga). Las capturas de cada pantalla están en `docs/capturas/`.

| ID | CU | Entrada | Resultado esperado | Obtenido | Estado |
|---|---|---|---|---|---|
| CP-M01 | CU-01 | Clave incorrecta | Mensaje único de credenciales | "Usuario o clave incorrectos." | Aprobada |
| CP-M02 | CU-01 / interceptor | `GET /menu` sin sesión | Redirige a login | 302 a `/login` | Aprobada |
| CP-M03 | RN-08 | Recepcionista en `/reportes` | 403 | 403 (captura 15) | Aprobada |
| CP-M04 | RN-08 | Administrador en `/socios/registro` | 403 | 403 | Aprobada |
| CP-M05 | CU-04 | Recepcionista valida DNI 70000001 (vigente) | AUTORIZADO | AUTORIZADO (captura 03) | Aprobada |
| CP-M06 | CU-04 | Valida DNI 70000007 (vencido) | DENEGADO, "Membresía vencida" | Igual (captura 04) | Aprobada |
| CP-M07 | CU-04 aviso | Valida DNI 70000004 (vence en pocos días) | AUTORIZADO con aviso | Ver captura 05 | Aprobada |
| CP-M08 | CU-04 | Valida DNI 99999999 | "Socio no encontrado.", sin registro | Igual (captura 06); asistencias sin cambio | Aprobada |
| CP-M09 | CU-05 | Busca "ana" | Lista con nombre, DNI y teléfono | Igual (captura 07) | Aprobada |
| CP-M10 | CU-02 | Alta de socio nuevo con plan y medio | Socio, membresía que empieza hoy y pago de 1.00 | Confirmado en BD y en captura 08 | Aprobada |
| CP-M11 | CU-02 | Alta con DNI ya existente | Mensaje de DNI repetido | Igual | Aprobada |
| CP-M12 | CU-03 / RN-04 | Renovar socio vencido | Inicio = hoy | Confirmado en BD (inicio 2026-11-20) y captura 09 | Aprobada |
| CP-M13 | CU-03 / RN-04 | Renovar socio vigente (vence 2026-12-21) | Inicio = 2026-12-22 | Confirmado en BD | Aprobada |
| CP-M14 | CU-03 | DNI inexistente | "No existe un socio con ese DNI." | Igual | Aprobada |
| CP-M15 | CU-07 | Historial de 70000002 | Pago de 1.00 con periodo y usuario | Igual (captura 10) | Aprobada |
| CP-M16 | CU-08 / RN-07 | Lista de por vencer | Socios con vencimiento en 0..7 días, excluidos los vencidos | 3 socios (Elena 3, Diego 5, Fabio 6), contador 3 (captura 11) | Aprobada |
| CP-M17 | CU-09 | Reportes del mes | Socios activos, ingresos del mes, por vencer | Igual (captura 13); los valores reflejan las altas de las pruebas, ver nota | Aprobada |
| CP-M18 | CU-09 | Mes "2026-13" | Mensaje de formato | Igual (captura 14) | Aprobada |
| CP-M19 | RNF-02 | 100 validaciones de acceso seguidas (4 DNI rotando) | Medición en ms en el log | Promedio 3.15 ms, mediana 2.91 ms, mínimo 1.94 ms, máximo 19.12 ms (primera llamada) | Aprobada (dato medido, no garantizado) |
| CP-M20 | RNF-01 | Menú en pantalla de 390 px | Sin scroll horizontal | Sin scroll horizontal (captura 16 y 17) | Aprobada |

## C. Prueba de la propia prueba de rollback

Para comprobar que CP-23 y CP-24 realmente detectan un fallo de transacción, se quitó temporalmente `@Transactional` de `SocioServicio.registrar`:

- **Resultado:** CP-23 falló con `expected: <0> but was: <1>` (quedaba un socio huérfano).
- Después se restauró la anotación y CP-23 volvió a aprobar.

## Notas

0. **Tarifas.** Las ejecuciones manuales y de integración de las secciones A y B se hicieron con la tarifa provisional de 1.00 por plan. Después se fijó la tarifa vigente (Mensual 60.00, Trimestral 150.00). CP-31 ahora calcula el ingreso esperado a partir del precio del plan y se volvió a ejecutar con la tarifa nueva: aprobada. Los montos de 1.00 que aparecen en los resultados obtenidos corresponden a esa ejecución.

1. **Datos de las capturas.** Las capturas 08 y 09 registraron al socio Irene y renovaron a Gloria mientras se tomaban. Por eso el reporte muestra 8 socios activos y 4.00 de ingresos (los datos de la demostración más esas dos operaciones).
2. **Dato de rendimiento.** El tiempo medido corresponde al método `AccesoServicio.validarAcceso` completo (consultas y escritura), no a la petición HTTP. Es un valor a validar con el administrador.
3. **Regla de fin de mes (RN-02).** CP-03 documenta el resultado de la regla tal como está. Pendiente de confirmar con el administrador.
