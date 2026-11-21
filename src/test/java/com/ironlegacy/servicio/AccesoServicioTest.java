package com.ironlegacy.servicio;

import com.ironlegacy.dao.AsistenciaDao;
import com.ironlegacy.dao.MembresiaDao;
import com.ironlegacy.dao.SocioDao;
import com.ironlegacy.modelo.Asistencia;
import com.ironlegacy.modelo.Membresia;
import com.ironlegacy.modelo.ResultadoAcceso;
import com.ironlegacy.modelo.Rol;
import com.ironlegacy.modelo.Socio;
import com.ironlegacy.modelo.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** CU-04: los cuatro caminos (autorizado, denegado, sin membresía, socio no existe) más el aviso de vencimiento. */
@ExtendWith(MockitoExtension.class)
class AccesoServicioTest {

    // 15-jun-2026 10:00 en Lima (UTC-5).
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-06-15T15:00:00Z"), ZoneId.of("America/Lima"));
    private static final LocalDate HOY = LocalDate.of(2026, 6, 15);
    private static final String DNI = "12345678";

    @Mock
    private SocioDao socioDao;
    @Mock
    private MembresiaDao membresiaDao;
    @Mock
    private AsistenciaDao asistenciaDao;

    private AccesoServicio servicio;
    private Usuario recepcionista;
    private Socio socio;

    @BeforeEach
    void preparar() {
        servicio = new AccesoServicio(socioDao, membresiaDao, asistenciaDao, RELOJ);
        recepcionista = new Usuario(7L, "Recep", "recep", "hash", Rol.RECEPCIONISTA, true);
        socio = new Socio(10L, "Ana Torres", DNI, "987654321", LocalDate.of(2026, 1, 1));
    }

    @Test
    void autorizaSiLaMembresiaEstaVigenteYRegistraAsistencia() {
        cuandoElSocioTieneUltimaMembresiaQueVence(HOY.plusDays(30));

        var resultado = servicio.validarAcceso(DNI, recepcionista);

        assertEquals(ResultadoAcceso.AUTORIZADO, resultado.resultado());
        assertNull(resultado.motivo());
        assertNull(resultado.aviso());
        Asistencia registrada = capturarAsistencia();
        assertEquals(ResultadoAcceso.AUTORIZADO, registrada.resultado());
        assertNull(registrada.motivoDenegacion());
        assertEquals(10L, registrada.idSocio());
        assertEquals(7L, registrada.idUsuario());
    }

    @Test
    void deniegaConMotivoMembresiaVencidaYRegistraAsistencia() {
        cuandoElSocioTieneUltimaMembresiaQueVence(HOY.minusDays(1));

        var resultado = servicio.validarAcceso(DNI, recepcionista);

        assertEquals(ResultadoAcceso.DENEGADO, resultado.resultado());
        assertEquals("Membresía vencida", resultado.motivo());
        Asistencia registrada = capturarAsistencia();
        assertEquals(ResultadoAcceso.DENEGADO, registrada.resultado());
        assertEquals("Membresía vencida", registrada.motivoDenegacion());
    }

    @Test
    void deniegaConMotivoSinMembresiaYRegistraAsistencia() {
        when(socioDao.buscarPorDni(DNI)).thenReturn(Optional.of(socio));
        when(membresiaDao.obtenerUltima(socio)).thenReturn(Optional.empty());

        var resultado = servicio.validarAcceso(DNI, recepcionista);

        assertEquals(ResultadoAcceso.DENEGADO, resultado.resultado());
        assertEquals("Sin membresía", resultado.motivo());
        assertEquals("Sin membresía", capturarAsistencia().motivoDenegacion());
    }

    @Test
    void socioNoExisteNoRegistraNada() {
        when(socioDao.buscarPorDni(DNI)).thenReturn(Optional.empty());

        NegocioException error = assertThrows(NegocioException.class,
                () -> servicio.validarAcceso(DNI, recepcionista));

        assertEquals("Socio no encontrado.", error.getMessage());
        verify(asistenciaDao, never()).registrar(any());
    }

    @Test
    void avisaCuandoLaMembresiaVenceEnSieteDiasOMenos() {
        cuandoElSocioTieneUltimaMembresiaQueVence(HOY.plusDays(7));

        var resultado = servicio.validarAcceso(DNI, recepcionista);

        assertEquals(ResultadoAcceso.AUTORIZADO, resultado.resultado());
        assertEquals("La membresía vence en 7 día(s).", resultado.aviso());
    }

    @Test
    void rechazaUnDniQueNoTieneOchoDigitosSinConsultarLaBase() {
        assertThrows(NegocioException.class, () -> servicio.validarAcceso("1234", recepcionista));

        verify(socioDao, never()).buscarPorDni(any());
        verify(asistenciaDao, never()).registrar(any());
    }

    private void cuandoElSocioTieneUltimaMembresiaQueVence(LocalDate vencimiento) {
        when(socioDao.buscarPorDni(DNI)).thenReturn(Optional.of(socio));
        when(membresiaDao.obtenerUltima(socio)).thenReturn(Optional.of(
                new Membresia(5L, 10L, 1L, vencimiento.minusDays(30), vencimiento)));
    }

    private Asistencia capturarAsistencia() {
        ArgumentCaptor<Asistencia> captor = ArgumentCaptor.forClass(Asistencia.class);
        verify(asistenciaDao).registrar(captor.capture());
        return captor.getValue();
    }
}
