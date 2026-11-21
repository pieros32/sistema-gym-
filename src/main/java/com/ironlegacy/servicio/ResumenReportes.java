package com.ironlegacy.servicio;

import java.math.BigDecimal;
import java.time.YearMonth;

public record ResumenReportes(YearMonth mes, int sociosActivos, BigDecimal ingresosDelMes, int porVencer) {
}
