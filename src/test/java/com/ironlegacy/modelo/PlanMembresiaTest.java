package com.ironlegacy.modelo;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlanMembresiaTest {

    private static final BigDecimal PRECIO = new BigDecimal("1.00");

    @Test
    void mensualVenceUnDiaAntesDeUnMesDesdeElInicio() {
        PlanMembresia mensual = new PlanMembresia(1L, "Mensual", 1, PRECIO);

        assertEquals(LocalDate.of(2026, 1, 31), mensual.calcularVencimiento(LocalDate.of(2026, 1, 1)));
    }

    @Test
    void trimestralVenceUnDiaAntesDeTresMeses() {
        PlanMembresia trimestral = new PlanMembresia(2L, "Trimestral", 3, PRECIO);

        assertEquals(LocalDate.of(2026, 12, 31), trimestral.calcularVencimiento(LocalDate.of(2026, 10, 1)));
    }

    /**
     * Caso borde de RN-02: con inicio el 31 de enero, plusMonths lleva a 28 de febrero.
     * La regla se mantiene tal cual y el resultado es 27 de febrero (28 días contando ambos extremos).
     * Queda documentado en el informe para confirmar con el administrador.
     */
    @Test
    void finDeMesAcortaElVencimientoAlMismoDiaDelMesSiguienteMenosUno() {
        PlanMembresia mensual = new PlanMembresia(1L, "Mensual", 1, PRECIO);

        assertEquals(LocalDate.of(2026, 2, 27), mensual.calcularVencimiento(LocalDate.of(2026, 1, 31)));
    }
}
