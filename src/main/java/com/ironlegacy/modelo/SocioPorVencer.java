package com.ironlegacy.modelo;

import java.time.LocalDate;

/** Membresía que vence pronto y cuyo socio no tiene una posterior (RN-07). */
public record SocioPorVencer(String nombreCompleto, String telefono, LocalDate fechaVencimiento) {
}
