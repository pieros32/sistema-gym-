package com.ironlegacy.modelo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * El estado (vigente o vencida) no se guarda: se calcula con la fecha de referencia.
 */
public record Membresia(
        Long idMembresia,
        Long idSocio,
        Long idPlan,
        LocalDate fechaInicio,
        LocalDate fechaVencimiento) {

    public Membresia {
        if (!fechaVencimiento.isAfter(fechaInicio)) {
            throw new IllegalArgumentException("El vencimiento debe ser posterior al inicio de la membresía.");
        }
    }

    /** RN-03: vigente hasta la fecha de vencimiento inclusive. */
    public boolean esVigente(LocalDate fecha) {
        return !fecha.isAfter(fechaVencimiento);
    }

    public int diasParaVencer(LocalDate fecha) {
        return (int) ChronoUnit.DAYS.between(fecha, fechaVencimiento);
    }
}
