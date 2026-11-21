package com.ironlegacy.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Fila del historial de pagos (CU-07): el pago con el plan, el periodo y quién lo registró. */
public record PagoDetalle(
        LocalDate fechaPago,
        BigDecimal monto,
        MedioPago medioPago,
        String nombrePlan,
        LocalDate fechaInicio,
        LocalDate fechaVencimiento,
        String nombreUsuario) {
}
