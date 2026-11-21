package com.ironlegacy.servicio;

import com.ironlegacy.dao.MembresiaDao;
import com.ironlegacy.dao.PagoDao;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

/** CU-09. Los datos salen de consultas agregadas en los DAO; aquí solo se componen. */
@Service
public class ReporteServicio {

    private final MembresiaDao membresiaDao;
    private final PagoDao pagoDao;
    private final MembresiaServicio membresiaServicio;
    private final Clock reloj;

    public ReporteServicio(MembresiaDao membresiaDao, PagoDao pagoDao,
                           MembresiaServicio membresiaServicio, Clock reloj) {
        this.membresiaDao = membresiaDao;
        this.pagoDao = pagoDao;
        this.membresiaServicio = membresiaServicio;
        this.reloj = reloj;
    }

    /** Mes en formato AAAA-MM; si es nulo o vacío, el mes actual. */
    public ResumenReportes resumen(String mesTexto) {
        YearMonth mes = parsearMes(mesTexto);
        LocalDate hoy = LocalDate.now(reloj);
        return new ResumenReportes(
                mes,
                membresiaDao.contarSociosActivos(hoy),
                pagoDao.sumarIngresos(mes.atDay(1), mes.atEndOfMonth()),
                membresiaServicio.proximosAVencer().size());
    }

    private YearMonth parsearMes(String mesTexto) {
        if (mesTexto == null || mesTexto.isBlank()) {
            return YearMonth.now(reloj);
        }
        try {
            return YearMonth.parse(mesTexto.trim());
        } catch (DateTimeParseException e) {
            throw new NegocioException("El mes debe tener el formato AAAA-MM.");
        }
    }
}
