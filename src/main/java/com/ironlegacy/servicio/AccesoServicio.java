package com.ironlegacy.servicio;

import com.ironlegacy.dao.AsistenciaDao;
import com.ironlegacy.dao.MembresiaDao;
import com.ironlegacy.dao.SocioDao;
import com.ironlegacy.modelo.Asistencia;
import com.ironlegacy.modelo.Membresia;
import com.ironlegacy.modelo.ResultadoAcceso;
import com.ironlegacy.modelo.Socio;
import com.ironlegacy.modelo.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Optional;

/**
 * CU-04. Todo intento con socio registrado queda en asistencia (RN-06); si el DNI
 * no existe, no se registra nada.
 */
@Service
public class AccesoServicio {

    private static final Logger log = LoggerFactory.getLogger(AccesoServicio.class);
    private static final int DIAS_DE_AVISO = 7;

    private final SocioDao socioDao;
    private final MembresiaDao membresiaDao;
    private final AsistenciaDao asistenciaDao;
    private final Clock reloj;

    public AccesoServicio(SocioDao socioDao, MembresiaDao membresiaDao, AsistenciaDao asistenciaDao, Clock reloj) {
        this.socioDao = socioDao;
        this.membresiaDao = membresiaDao;
        this.asistenciaDao = asistenciaDao;
        this.reloj = reloj;
    }

    public ResultadoValidacion validarAcceso(String dni, Usuario recepcionista) {
        long inicioNs = System.nanoTime();

        String dniLimpio = dni == null ? "" : dni.trim();
        if (!dniLimpio.matches("\\d{8}")) {
            throw new NegocioException("El DNI debe tener 8 dígitos.");
        }
        Socio socio = socioDao.buscarPorDni(dniLimpio)
                .orElseThrow(() -> new NegocioException("Socio no encontrado."));

        LocalDate hoy = LocalDate.now(reloj);
        Optional<Membresia> ultima = membresiaDao.obtenerUltima(socio);
        String motivo = motivoDenegacion(ultima, hoy);
        ResultadoAcceso resultado = motivo == null ? ResultadoAcceso.AUTORIZADO : ResultadoAcceso.DENEGADO;

        asistenciaDao.registrar(new Asistencia(
                null,
                socio.idSocio(),
                recepcionista.idUsuario(),
                LocalDateTime.now(reloj).truncatedTo(ChronoUnit.SECONDS),
                resultado,
                motivo));

        double ms = (System.nanoTime() - inicioNs) / 1_000_000.0;
        log.info("validarAcceso socioId={} resultado={} ms={}",
                socio.idSocio(), resultado, String.format(Locale.ROOT, "%.2f", ms));

        String aviso = resultado == ResultadoAcceso.AUTORIZADO ? avisoDeVencimiento(ultima.orElseThrow(), hoy) : null;
        return new ResultadoValidacion(socio, resultado, motivo, aviso);
    }

    private static String motivoDenegacion(Optional<Membresia> ultima, LocalDate hoy) {
        if (ultima.isEmpty()) {
            return "Sin membresía";
        }
        return ultima.get().esVigente(hoy) ? null : "Membresía vencida";
    }

    private static String avisoDeVencimiento(Membresia membresia, LocalDate hoy) {
        int dias = membresia.diasParaVencer(hoy);
        if (dias > DIAS_DE_AVISO) {
            return null;
        }
        return dias == 0 ? "La membresía vence hoy." : "La membresía vence en " + dias + " día(s).";
    }

    /** Resultado de una validación: motivo y aviso son nulos cuando no aplican. */
    public record ResultadoValidacion(Socio socio, ResultadoAcceso resultado, String motivo, String aviso) {
    }
}
