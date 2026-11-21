package com.ironlegacy.integracion;

import com.ironlegacy.dao.PagoDao;
import com.ironlegacy.dao.UsuarioDao;
import com.ironlegacy.modelo.MedioPago;
import com.ironlegacy.modelo.Membresia;
import com.ironlegacy.modelo.Usuario;
import com.ironlegacy.servicio.DatosRegistroSocio;
import com.ironlegacy.servicio.MembresiaServicio;
import com.ironlegacy.servicio.NegocioException;
import com.ironlegacy.servicio.ResultadoOperacion;
import com.ironlegacy.servicio.SocioServicio;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Clock;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

/**
 * CU-02, CU-03 y CU-06 contra MySQL real. Los mocks no ejecutan @Transactional, así que el
 * rollback solo se demuestra aquí. Para forzar el fallo del pago, un SpyBean sobre PagoDao lanza
 * una excepción al insertar. Requiere la variable DB_CLAVE; sin ella la clase se omite.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "spring.datasource.url=jdbc:mysql://localhost:3306/iron_legacy_test"
                + "?serverTimezone=America/Lima&useSSL=false&allowPublicKeyRetrieval=true")
@EnabledIfEnvironmentVariable(named = "DB_CLAVE", matches = ".+")
class RegistroRenovacionIntegracionTest {

    private static final String SERVIDOR =
            "jdbc:mysql://localhost:3306/?serverTimezone=America/Lima&useSSL=false&allowPublicKeyRetrieval=true";
    private static final String BASE_ORIGINAL = "iron_legacy";
    private static final String BASE_DE_PRUEBA = "iron_legacy_test";

    @Autowired
    private SocioServicio socioServicio;
    @Autowired
    private MembresiaServicio membresiaServicio;
    @Autowired
    private UsuarioDao usuarioDao;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private Clock reloj;
    @SpyBean
    private PagoDao pagoDao;

    private Usuario recepcionista;
    private Long idPlanMensual;

    /** Recrea la base de prueba desde los mismos scripts que usa la aplicación, solo cambiando el nombre. */
    @BeforeAll
    static void recrearBaseDePrueba() throws Exception {
        try (Connection conexion = DriverManager.getConnection(SERVIDOR, "iron_legacy", System.getenv("DB_CLAVE"))) {
            ejecutar(conexion, "DROP DATABASE IF EXISTS " + BASE_DE_PRUEBA + ";");
            ejecutar(conexion, cambiarBase(Files.readString(Path.of("db/schema.sql"))));
            ejecutar(conexion, cambiarBase(Files.readString(Path.of("db/seed.sql"))));
        }
    }

    @BeforeEach
    void limpiarTablasDeNegocio() {
        jdbc.update("DELETE FROM pago");
        jdbc.update("DELETE FROM membresia");
        jdbc.update("DELETE FROM asistencia");
        jdbc.update("DELETE FROM socio");
        recepcionista = usuarioDao.buscarPorNombreUsuario("recepcion").orElseThrow();
        idPlanMensual = jdbc.queryForObject("SELECT id_plan FROM plan_membresia WHERE nombre = 'Mensual'", Long.class);
    }

    @Test
    void registrarCreaSocioMembresiaYPagoConLasFechasDelPlan() {
        ResultadoOperacion resultado = socioServicio.registrar(datosDeSocio("12345678"), recepcionista);

        LocalDate hoy = LocalDate.now(reloj);
        assertEquals(hoy, resultado.membresia().fechaInicio());
        assertEquals(hoy.plusMonths(1).minusDays(1), resultado.membresia().fechaVencimiento());
        assertEquals(MedioPago.YAPE, resultado.pago().medioPago());
        assertEquals(1, contar("socio"));
        assertEquals(1, contar("membresia"));
        assertEquals(1, contar("pago"));
    }

    @Test
    void siFallaElPagoElAltaNoDejaNingunRegistro() {
        doThrow(new DataAccessResourceFailureException("Fallo simulado al insertar el pago"))
                .when(pagoDao).crear(any());

        assertThrows(DataAccessException.class,
                () -> socioServicio.registrar(datosDeSocio("22223333"), recepcionista));

        assertEquals(0, contar("socio"));
        assertEquals(0, contar("membresia"));
        assertEquals(0, contar("pago"));
    }

    @Test
    void siFallaElPagoLaRenovacionNoDejaMembresiaNiPagoNuevos() {
        socioServicio.registrar(datosDeSocio("33334444"), recepcionista);
        doThrow(new DataAccessResourceFailureException("Fallo simulado al insertar el pago"))
                .when(pagoDao).crear(any());

        assertThrows(DataAccessException.class,
                () -> membresiaServicio.renovar("33334444", idPlanMensual, MedioPago.EFECTIVO, recepcionista));

        assertEquals(1, contar("membresia"));
        assertEquals(1, contar("pago"));
    }

    @Test
    void renovarUnaMembresiaVigenteEmpiezaElDiaSiguienteAlVencimiento() {
        ResultadoOperacion alta = socioServicio.registrar(datosDeSocio("44445555"), recepcionista);

        ResultadoOperacion renovacion = membresiaServicio.renovar(
                "44445555", idPlanMensual, MedioPago.PLIN, recepcionista);

        Membresia primera = alta.membresia();
        assertEquals(primera.fechaVencimiento().plusDays(1), renovacion.membresia().fechaInicio());
        assertEquals(2, contar("membresia"));
        assertEquals(2, contar("pago"));
    }

    @Test
    void noPermiteDosAltasConElMismoDni() {
        socioServicio.registrar(datosDeSocio("55556666"), recepcionista);

        assertThrows(NegocioException.class,
                () -> socioServicio.registrar(datosDeSocio("55556666"), recepcionista));

        assertEquals(1, contar("socio"));
    }

    @Test
    void noRenuevaUnDniQueNoExiste() {
        NegocioException error = assertThrows(NegocioException.class,
                () -> membresiaServicio.renovar("99998888", idPlanMensual, MedioPago.EFECTIVO, recepcionista));

        assertEquals("No existe un socio con ese DNI.", error.getMessage());
        assertEquals(0, contar("membresia"));
    }

    private DatosRegistroSocio datosDeSocio(String dni) {
        return new DatosRegistroSocio("Socio de prueba", dni, "900000000", idPlanMensual, MedioPago.YAPE);
    }

    private int contar(String tabla) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + tabla, Integer.class);
    }

    private static String cambiarBase(String sql) {
        return sql.replace(BASE_ORIGINAL, BASE_DE_PRUEBA);
    }

    private static void ejecutar(Connection conexion, String sql) {
        ScriptUtils.executeSqlScript(conexion,
                new EncodedResource(new ByteArrayResource(sql.getBytes(StandardCharsets.UTF_8))));
    }
}
