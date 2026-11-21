package com.ironlegacy.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PlanMembresia(
        Long idPlan,
        String nombre,
        int duracionMeses,
        BigDecimal precio) {

    /** RN-02: el vencimiento es el inicio más la duración, menos un día. */
    public LocalDate calcularVencimiento(LocalDate inicio) {
        return inicio.plusMonths(duracionMeses).minusDays(1);
    }
}
