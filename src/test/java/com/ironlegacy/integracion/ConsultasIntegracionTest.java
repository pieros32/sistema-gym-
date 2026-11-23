package com.ironlegacy.integracion;

import com.ironlegacy.dao.UsuarioDao;
import com.ironlegacy.modelo.MedioPago;
import com.ironlegacy.modelo.Usuario;
import com.ironlegacy.servicio.DatosRegistroSocio;
import com.ironlegacy.servicio.MembresiaServicio;
import com.ironlegacy.servicio.NegocioException;
import com.ironlegacy.servicio.PagoServicio;
import com.ironlegacy.servicio.ReporteServicio;
import com.ironlegacy.servicio.SocioServicio;
import com.ironlegacy.servicio.VencimientoProximo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** CU-07, CU-08 (con RN-07 en SQL) y CU-09 contra MySQL real. */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "spring.datasource.url=" + BaseDePruebas.URL_BASE)
@EnabledIfEnvironmentVariable(named = "DB_CLAVE", matches = ".+")
class ConsultasIntegracionTest {

    @Autowired
    private SocioServicio socioServicio;
    @Autowired
    private MembresiaServicio membresiaServicio;
    @Autowired
    private PagoServicio pagoServicio;
    @Autowired
    private ReporteServicio reporteServicio;
    @Autowired
    private UsuarioDao usuarioDao;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private Clock reloj;

    private Usuario recepcionista;
    private Long idPlanMensual;

    @BeforeAll
    static void prepararBase() throws Exception {
        BaseDePruebas.preparar();
    }

    @BeforeEach
    void limpiar() {
        BaseDePruebas.limpiar(jdbc);
        recepcionista = usuarioDao.buscarPorNombreUsuario("recepcion").orElseThrow();
        idPlanMensual = jdbc.queryForObject("SELECT id_plan FROM plan_membresia WHERE nombre = 'Mensual'", Long.class);
    }

    @Test
    void porVencerIncluyeLasVentanaDeSieteDiasYExcluyeLosSociosQueYaRenovaron() {
        LocalDate hoy = LocalDate.now(reloj);
        long idA = insertarSocio("10000001", "Socio A");
        insertarMembresia(idA, hoy.minusDays(27), hoy.plusDays(3));  // vence pronto...
        insertarMembresia(idA, hoy.plusDays(4), hoy.plusDays(40));   // ...pero tiene una posterior: RN-07 la excluye
        insertarMembresia(insertarSocio("10000002", "Socio B"), hoy.minusDays(25), hoy.plusDays(5));
        insertarMembresia(insertarSocio("10000003", "Socio C"), hoy.minusDays(20), hoy.plusDays(8));  // fuera de la ventana
        insertarMembresia(insertarSocio("10000004", "Socio D"), hoy.minusDays(30), hoy.minusDays(1)); // ya venció
        insertarMembresia(insertarSocio("10000005", "Socio E"), hoy.minusDays(29), hoy);              // vence hoy

        List<VencimientoProximo> proximos = membresiaServicio.proximosAVencer();

        assertEquals(2, proximos.size());
        assertEquals("Socio E", proximos.get(0).nombreCompleto());
        assertEquals(0, proximos.get(0).diasRestantes());
        assertEquals("Socio B", proximos.get(1).nombreCompleto());
        assertEquals(5, proximos.get(1).diasRestantes());
    }

    @Test
    void historialDelSocioVaDelPagoMasRecienteAlMasAntiguo() {
        socioServicio.registrar(datosDeSocio("20000001"), recepcionista);
        var renovacion = membresiaServicio.renovar("20000001", idPlanMensual, MedioPago.PLIN, recepcionista);

        var historial = pagoServicio.historialPorDni("20000001");

        assertEquals(2, historial.pagos().size());
        var masReciente = historial.pagos().get(0);
        assertEquals(MedioPago.PLIN, masReciente.medioPago());
        assertEquals(renovacion.membresia().fechaInicio(), masReciente.fechaInicio());
        assertEquals("Mensual", masReciente.nombrePlan());
        assertEquals("Recepcionista Demo", masReciente.nombreUsuario());
    }

    @Test
    void historialRechazaUnDniQueNoExiste() {
        NegocioException error = assertThrows(NegocioException.class,
                () -> pagoServicio.historialPorDni("20000009"));

        assertEquals("No existe un socio con ese DNI.", error.getMessage());
    }

    @Test
    void reportesCuentanSociosActivosIngresosDelMesYPorVencer() {
        socioServicio.registrar(datosDeSocio("30000001"), recepcionista);
        socioServicio.registrar(datosDeSocio("30000002"), recepcionista);
        String mesActual = YearMonth.now(reloj).toString();
        String mesAnterior = YearMonth.now(reloj).minusMonths(1).toString();

        var actual = reporteServicio.resumen(mesActual);
        var anterior = reporteServicio.resumen(mesAnterior);

        assertEquals(2, actual.sociosActivos());
        BigDecimal precioMensual = jdbc.queryForObject(
                "SELECT precio FROM plan_membresia WHERE nombre = 'Mensual'", BigDecimal.class);
        assertEquals(0, precioMensual.multiply(new BigDecimal("2")).compareTo(actual.ingresosDelMes()));
        assertEquals(0, actual.porVencer());
        assertEquals(0, BigDecimal.ZERO.compareTo(anterior.ingresosDelMes()));
    }

    @Test
    void reportesRechazanUnMesMalFormado() {
        NegocioException error = assertThrows(NegocioException.class,
                () -> reporteServicio.resumen("2026-13"));

        assertEquals("El mes debe tener el formato AAAA-MM.", error.getMessage());
    }

    private DatosRegistroSocio datosDeSocio(String dni) {
        return new DatosRegistroSocio("Socio de prueba", dni, "900000000", idPlanMensual, MedioPago.YAPE);
    }

    private long insertarSocio(String dni, String nombre) {
        jdbc.update("INSERT INTO socio (nombre_completo, dni, telefono, fecha_registro) VALUES (?, ?, '900000000', ?)",
                nombre, dni, LocalDate.now(reloj));
        return jdbc.queryForObject("SELECT id_socio FROM socio WHERE dni = ?", Long.class, dni);
    }

    private void insertarMembresia(long idSocio, LocalDate inicio, LocalDate vencimiento) {
        jdbc.update("INSERT INTO membresia (id_socio, id_plan, fecha_inicio, fecha_vencimiento) VALUES (?, ?, ?, ?)",
                idSocio, idPlanMensual, inicio, vencimiento);
    }
}
