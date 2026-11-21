package com.ironlegacy.dao;

import com.ironlegacy.modelo.PlanMembresia;

import java.util.List;
import java.util.Optional;

public interface PlanMembresiaDao {

    List<PlanMembresia> listar();

    Optional<PlanMembresia> obtenerPorId(Long idPlan);
}
