package com.ironlegacy.modelo;

import java.util.Set;

/** Matriz de permisos por rol (RN-08). Fuente única de verdad para el interceptor. */
public enum Rol {

    RECEPCIONISTA(Set.of("CU-01", "CU-02", "CU-03", "CU-04", "CU-05", "CU-06", "CU-07", "CU-08")),
    ADMINISTRADOR(Set.of("CU-01", "CU-05", "CU-07", "CU-08", "CU-09"));

    private final Set<String> casosDeUso;

    Rol(Set<String> casosDeUso) {
        this.casosDeUso = casosDeUso;
    }

    public boolean puede(String casoDeUso) {
        return casosDeUso.contains(casoDeUso);
    }
}
