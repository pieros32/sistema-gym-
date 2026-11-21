package com.ironlegacy.dao;

import com.ironlegacy.modelo.Membresia;
import com.ironlegacy.modelo.Socio;

import java.util.Optional;

public interface MembresiaDao {

    /** La membresía con la fecha de vencimiento más lejana del socio. */
    Optional<Membresia> obtenerUltima(Socio socio);

    Membresia crear(Membresia membresia);
}
