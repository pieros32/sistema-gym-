package com.ironlegacy.servicio;

import com.ironlegacy.dao.SocioDao;
import com.ironlegacy.modelo.MedioPago;
import com.ironlegacy.modelo.Rol;
import com.ironlegacy.modelo.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** CU-02: validaciones de entrada y rechazo de DNI repetido, sin tocar la base de datos. */
@ExtendWith(MockitoExtension.class)
class SocioServicioTest {

    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-06-15T15:00:00Z"), ZoneId.of("America/Lima"));

    @Mock
    private SocioDao socioDao;
    @Mock
    private MembresiaServicio membresiaServicio;
    @Mock
    private PagoServicio pagoServicio;

    private SocioServicio servicio;
    private Usuario recepcionista;

    @BeforeEach
    void preparar() {
        servicio = new SocioServicio(socioDao, membresiaServicio, pagoServicio, RELOJ);
        recepcionista = new Usuario(7L, "Recep", "recep", "hash", Rol.RECEPCIONISTA, true);
    }

    @Test
    void rechazaUnDniDeSieteDigitos() {
        var datos = new DatosRegistroSocio("Ana Torres", "1234567", "987654321", 1L, MedioPago.EFECTIVO);

        NegocioException error = assertThrows(NegocioException.class, () -> servicio.registrar(datos, recepcionista));

        assertEquals("El DNI debe tener 8 dígitos.", error.getMessage());
        verify(socioDao, never()).crear(any());
    }

    @Test
    void rechazaUnTelefonoQueNoTieneNueveDigitos() {
        var datos = new DatosRegistroSocio("Ana Torres", "12345678", "98765", 1L, MedioPago.YAPE);

        NegocioException error = assertThrows(NegocioException.class, () -> servicio.registrar(datos, recepcionista));

        assertEquals("El teléfono debe tener 9 dígitos.", error.getMessage());
    }

    @Test
    void rechazaUnDniQueYaExisteSinCrearNada() {
        when(socioDao.existePorDni("12345678")).thenReturn(true);
        var datos = new DatosRegistroSocio("Ana Torres", "12345678", "987654321", 1L, MedioPago.PLIN);

        NegocioException error = assertThrows(NegocioException.class, () -> servicio.registrar(datos, recepcionista));

        assertEquals(
                "Ya existe un socio con ese DNI. Si quiere renovar su membresía, use Renovar membresía.",
                error.getMessage());
        verify(socioDao, never()).crear(any());
        verify(pagoServicio, never()).registrar(any(), any(), any(), any(), any());
    }
}
