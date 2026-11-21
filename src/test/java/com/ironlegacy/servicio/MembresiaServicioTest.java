package com.ironlegacy.servicio;

import com.ironlegacy.modelo.Membresia;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** RN-04: cómo se decide el inicio de una renovación. */
class MembresiaServicioTest {

    private static final LocalDate HOY = LocalDate.of(2026, 6, 15);

    @Test
    void renovarVigenteEmpiezaElDiaSiguienteAlVencimiento() {
        Membresia vigente = new Membresia(1L, 10L, 1L, HOY.minusDays(20), HOY.plusDays(10));

        LocalDate inicio = MembresiaServicio.inicioDeRenovacion(Optional.of(vigente), HOY);

        assertEquals(HOY.plusDays(11), inicio);
    }

    @Test
    void renovarQueVenceHoyEmpiezaMananaPorqueSigueVigenteHoy() {
        Membresia venceHoy = new Membresia(1L, 10L, 1L, HOY.minusDays(29), HOY);

        LocalDate inicio = MembresiaServicio.inicioDeRenovacion(Optional.of(venceHoy), HOY);

        assertEquals(HOY.plusDays(1), inicio);
    }

    @Test
    void renovarVencidaEmpiezaHoy() {
        Membresia vencida = new Membresia(1L, 10L, 1L, HOY.minusDays(40), HOY.minusDays(10));

        LocalDate inicio = MembresiaServicio.inicioDeRenovacion(Optional.of(vencida), HOY);

        assertEquals(HOY, inicio);
    }

    @Test
    void socioSinMembresiaEmpiezaHoy() {
        assertEquals(HOY, MembresiaServicio.inicioDeRenovacion(Optional.empty(), HOY));
    }
}
