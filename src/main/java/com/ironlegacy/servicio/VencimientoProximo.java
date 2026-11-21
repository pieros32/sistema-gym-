package com.ironlegacy.servicio;

import java.time.LocalDate;

public record VencimientoProximo(String nombreCompleto, String telefono, LocalDate fechaVencimiento, int diasRestantes) {
}
