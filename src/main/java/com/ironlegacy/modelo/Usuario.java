package com.ironlegacy.modelo;

/** Usuario del sistema (recepcionista o administrador). La clave solo se guarda como hash. */
public record Usuario(
        Long idUsuario,
        String nombreCompleto,
        String nombreUsuario,
        String claveHash,
        Rol rol,
        boolean activo) {

    public boolean tienePermiso(String casoDeUso) {
        return activo && rol.puede(casoDeUso);
    }
}
