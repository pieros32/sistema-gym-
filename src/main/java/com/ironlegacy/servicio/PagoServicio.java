package com.ironlegacy.servicio;

import com.ironlegacy.dao.PagoDao;
import com.ironlegacy.modelo.MedioPago;
import com.ironlegacy.modelo.Membresia;
import com.ironlegacy.modelo.Pago;
import com.ironlegacy.modelo.Usuario;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

/** CU-06. No tiene pantalla propia: lo invocan el alta de socio (CU-02) y la renovación (CU-03). */
@Service
public class PagoServicio {

    private final PagoDao pagoDao;

    public PagoServicio(PagoDao pagoDao) {
        this.pagoDao = pagoDao;
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
}
