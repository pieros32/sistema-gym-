package com.ironlegacy.servicio;

import com.ironlegacy.modelo.PagoDetalle;
import com.ironlegacy.modelo.Socio;

import java.util.List;

public record HistorialPagos(Socio socio, List<PagoDetalle> pagos) {
}
