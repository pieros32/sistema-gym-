package com.ironlegacy.config;

import com.ironlegacy.dao.SocioDao;
import com.ironlegacy.dao.UsuarioDao;
import com.ironlegacy.modelo.MedioPago;
import com.ironlegacy.modelo.Membresia;
import com.ironlegacy.modelo.PlanMembresia;
import com.ironlegacy.modelo.Socio;
import com.ironlegacy.modelo.Usuario;
import com.ironlegacy.servicio.MembresiaServicio;
import com.ironlegacy.servicio.PagoServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Socios de demostración con membresías vigentes, por vencer y vencidas. Solo corre con el perfil
 * demo y si no hay socios. Las fechas son relativas a hoy, así la demostración no caduca.
 */
@Component
@Profile("demo")
public class DatosDemo implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosDemo.class);

    /** Días transcurridos desde el inicio de la membresía hasta hoy. */
    private record SocioDemo(String nombre, String dni, String telefono, String plan, int diasDesdeInicio, MedioPago medio) {
    }

    private static final SocioDemo[] SOCIOS = {
            new SocioDemo("Ana Torres", "70000001", "911000001", "Mensual", 10, MedioPago.YAPE),
            new SocioDemo("Bruno Silva", "70000002", "911000002", "Trimestral", 30, MedioPago.EFECTIVO),
            new SocioDemo("Carla Mendoza", "70000003", "911000003", "Mensual", 5, MedioPago.PLIN),
            new SocioDemo("Diego Ramos", "70000004", "911000004", "Mensual", 25, MedioPago.YAPE),
            new SocioDemo("Elena Vargas", "70000005", "911000005", "Mensual", 27, MedioPago.EFECTIVO),
            new SocioDemo("Fabio Castro", "70000006", "911000006", "Trimestral", 85, MedioPago.PLIN),
            new SocioDemo("Gloria Núñez", "70000007", "911000007", "Mensual", 60, MedioPago.YAPE),
            new SocioDemo("Hugo Paredes", "70000008", "911000008", "Trimestral", 120, MedioPago.EFECTIVO)
    };

    private final SocioDao socioDao;
    private final UsuarioDao usuarioDao;
    private final MembresiaServicio membresiaServicio;
    private final PagoServicio pagoServicio;
    private final Clock reloj;

    public DatosDemo(SocioDao socioDao, UsuarioDao usuarioDao, MembresiaServicio membresiaServicio,
                     PagoServicio pagoServicio, Clock reloj) {
        this.socioDao = socioDao;
        this.usuarioDao = usuarioDao;
        this.membresiaServicio = membresiaServicio;
        this.pagoServicio = pagoServicio;
        this.reloj = reloj;
    }

    @Override
    public void run(String... args) {
        if (socioDao.contar() > 0) {
            log.info("Datos de demostración omitidos: ya existen socios.");
            return;
        }
        Usuario recepcionista = usuarioDao.buscarPorNombreUsuario("recepcion")
                .orElseThrow(() -> new IllegalStateException("Falta el usuario 'recepcion' en la semilla."));
        Map<String, PlanMembresia> planes = membresiaServicio.listarPlanes().stream()
                .collect(Collectors.toMap(PlanMembresia::nombre, plan -> plan));

        LocalDate hoy = LocalDate.now(reloj);
        for (SocioDemo demo : SOCIOS) {
            LocalDate inicio = hoy.minusDays(demo.diasDesdeInicio());
            PlanMembresia plan = planes.get(demo.plan());
            Socio socio = socioDao.crear(new Socio(null, demo.nombre(), demo.dni(), demo.telefono(), inicio));
            Membresia membresia = membresiaServicio.crear(socio.idSocio(), plan, inicio);
            pagoServicio.registrar(membresia, plan.precio(), demo.medio(), recepcionista, inicio);
        }
        log.info("Datos de demostración creados: {} socios.", SOCIOS.length);
    }
}
