package com.ironlegacy.dao;

import com.ironlegacy.modelo.Socio;

import java.util.List;
import java.util.Optional;

public interface SocioDao {

    Optional<Socio> buscarPorDni(String dni);

    List<Socio> buscarPorNombre(String fragmento);

    boolean existePorDni(String dni);

    Socio crear(Socio socio);
}
