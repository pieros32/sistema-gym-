package com.ironlegacy.servicio;

import com.ironlegacy.dao.PagoDao;
import com.ironlegacy.dao.SocioDao;
import com.ironlegacy.modelo.MedioPago;
import com.ironlegacy.modelo.Membresia;
import com.ironlegacy.modelo.Pago;
import com.ironlegacy.modelo.Socio;
import com.ironlegacy.modelo.Usuario;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

/** CU-06 (registrar pago) y CU-07 (historial). El pago no tiene pantalla propia: lo invocan CU-02 y CU-03. */
@Service
public class PagoServicio {

    private final PagoDao pagoDao;
    private final SocioDao socioDao;

    public PagoServicio(PagoDao pagoDao, SocioDao socioDao) {
        this.pagoDao = pagoDao;
        this.socioDao = socioDao;
    }

    public Pago registrar(Membresia membresia, BigDecimal monto, MedioPago medioPago,
                          Usuario usuario, LocalDate fecha) {
        if (monto == null || monto.signum() <= 0) {
            throw new NegocioException("El plan no tiene un precio válido.");
        }
        if (medioPago == null) {
            throw new NegocioException("Elige un medio de pago.");
        }
        return pagoDao.crear(new Pago(null, membresia.idMembresia(), usuario.idUsuario(),
                monto, fecha, medioPago));
    }

    public HistorialPagos historialPorDni(String dni) {
        String dniLimpio = dni == null ? "" : dni.trim();
        if (!dniLimpio.matches("\\d{8}")) {
            throw new NegocioException("El DNI debe tener 8 dígitos.");
        }
        Socio socio = socioDao.buscarPorDni(dniLimpio)
                .orElseThrow(() -> new NegocioException("No existe un socio con ese DNI."));
        return new HistorialPagos(socio, pagoDao.listarPorSocio(socio.idSocio()));
    }
}
