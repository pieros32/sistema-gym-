package com.ironlegacy.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

/** RN-05: cada membresía se paga con un único pago de monto positivo. */
public record Pago(
        Long idPago,
        Long idMembresia,
        Long idUsuario,
        BigDecimal monto,
        LocalDate fechaPago,
        MedioPago medioPago) {

    public Pago {
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor que cero.");
        }
    }
}
