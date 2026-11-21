package com.ironlegacy.servicio;

import com.ironlegacy.dao.MembresiaDao;
import com.ironlegacy.dao.PlanMembresiaDao;
import com.ironlegacy.dao.SocioDao;
import com.ironlegacy.modelo.SocioPorVencer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** RN-07 / CU-08: la ventana es de hoy a hoy+7 y se calculan los días que faltan. */
@ExtendWith(MockitoExtension.class)
class ProximosAVencerTest {

    private static final LocalDate HOY = LocalDate.of(2026, 6, 15);
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-06-15T15:00:00Z"), ZoneId.of("America/Lima"));

    @Mock
    private PlanMembresiaDao planMembresiaDao;
    @Mock
    private MembresiaDao membresiaDao;
    @Mock
    private SocioDao socioDao;
    @Mock
    private PagoServicio pagoServicio;

    @Test
    void consultaLaVentanaDeSieteDiasYCalculaLosDiasRestantes() {
        MembresiaServicio servicio = new MembresiaServicio(planMembresiaDao, membresiaDao, socioDao, pagoServicio, RELOJ);
        when(membresiaDao.buscarPorVencer(HOY, HOY.plusDays(7)))
                .thenReturn(List.of(new SocioPorVencer("Ana Torres", "987654321", HOY.plusDays(3))));

        List<VencimientoProximo> resultado = servicio.proximosAVencer();

        verify(membresiaDao).buscarPorVencer(HOY, HOY.plusDays(7));
        assertEquals(1, resultado.size());
        assertEquals(3, resultado.get(0).diasRestantes());
        assertEquals("Ana Torres", resultado.get(0).nombreCompleto());
    }
}
