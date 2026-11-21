package com.ironlegacy.servicio;

/**
 * Error de regla de negocio o de validación. Su mensaje va tal cual a la pantalla,
 * así que debe estar redactado para el personal de recepción.
 */
public class NegocioException extends RuntimeException {

    public NegocioException(String mensaje) {
        super(mensaje);
    }
}
