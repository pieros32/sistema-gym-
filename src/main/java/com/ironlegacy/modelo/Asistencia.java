package com.ironlegacy.modelo;

import java.time.LocalDateTime;

public record Asistencia(
        Long idAsistencia,
        Long idSocio,
        Long idUsuario,
        LocalDateTime fechaHora,
        ResultadoAcceso resultado,
        String motivoDenegacion) {

    public boolean esAutorizada() {
        return resultado == ResultadoAcceso.AUTORIZADO;
    }
}
