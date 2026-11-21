package com.ironlegacy.servicio;

import com.ironlegacy.modelo.MedioPago;

/** Datos que la recepción ingresa para dar de alta a un socio (CU-02). */
public record DatosRegistroSocio(
        String nombreCompleto,
        String dni,
        String telefono,
        Long idPlan,
        MedioPago medioPago) {
}
