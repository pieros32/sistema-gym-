package com.ironlegacy.servicio;

import com.ironlegacy.modelo.Membresia;
import com.ironlegacy.modelo.Pago;
import com.ironlegacy.modelo.Socio;

/** Resultado de un alta (CU-02) o una renovación (CU-03): lo que la pantalla confirma al usuario. */
public record ResultadoOperacion(Socio socio, Membresia membresia, Pago pago) {
}
