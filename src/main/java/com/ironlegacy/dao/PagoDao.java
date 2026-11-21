package com.ironlegacy.dao;

import com.ironlegacy.modelo.Pago;
import com.ironlegacy.modelo.PagoDetalle;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PagoDao {

    Pago crear(Pago pago);

    /** Pagos del socio, del más reciente al más antiguo. */
    List<PagoDetalle> listarPorSocio(Long idSocio);

    /** Suma de los pagos con fecha dentro del rango, ambos extremos incluidos. */
    BigDecimal sumarIngresos(LocalDate desde, LocalDate hasta);
}
