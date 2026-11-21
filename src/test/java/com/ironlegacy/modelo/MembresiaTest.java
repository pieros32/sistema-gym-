package com.ironlegacy.modelo;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MembresiaTest {

    private static final LocalDate HOY = LocalDate.of(2026, 6, 15);

    @Test
    void vigenteElMismoDiaDelVencimiento() {
        Membresia membresia = new Membresia(1L, 1L, 1L, LocalDate.of(2026, 5, 16), HOY);

        assertTrue(membresia.esVigente(HOY));
        assertEquals(0, membresia.diasParaVencer(HOY));
    }

    @Test
    void vencidaAlDiaSiguienteAlVencimiento() {
        Membresia membresia = new Membresia(1L, 1L, 1L, LocalDate.of(2026, 5, 16), HOY);

        assertFalse(membresia.esVigente(HOY.plusDays(1)));
    }

    @Test
    void diasParaVencerCuentaHastaElVencimiento() {
        Membresia membresia = new Membresia(1L, 1L, 1L, LocalDate.of(2026, 5, 16), HOY.plusDays(5));

        assertEquals(5, membresia.diasParaVencer(HOY));
    }

    @Test
    void rechazaUnVencimientoNoPosteriorAlInicio() {
        assertThrows(IllegalArgumentException.class,
                () -> new Membresia(1L, 1L, 1L, HOY, HOY));
    }
}
