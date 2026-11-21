package com.ironlegacy.modelo;

import java.time.LocalDate;

public record Socio(
        Long idSocio,
        String nombreCompleto,
        String dni,
        String telefono,
        LocalDate fechaRegistro) {
}
