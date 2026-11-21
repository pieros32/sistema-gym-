package com.ironlegacy.dao;

import com.ironlegacy.modelo.Membresia;
import com.ironlegacy.modelo.Socio;
import com.ironlegacy.modelo.SocioPorVencer;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MembresiaDao {

    /** La membresía con la fecha de vencimiento más lejana del socio. */
    Optional<Membresia> obtenerUltima(Socio socio);

    Membresia crear(Membresia membresia);

    /** RN-07: vencen entre ambas fechas y el socio no tiene una membresía posterior. */
    List<SocioPorVencer> buscarPorVencer(LocalDate desde, LocalDate hasta);

    /** Socios con una membresía que sigue vigente en la fecha dada (RN-03). */
    int contarSociosActivos(LocalDate fecha);
}
