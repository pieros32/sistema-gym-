package com.ironlegacy.dao.jdbc;

import com.ironlegacy.dao.PlanMembresiaDao;
import com.ironlegacy.modelo.PlanMembresia;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PlanMembresiaDaoJdbc implements PlanMembresiaDao {

    private static final String SELECT_PLAN = """
            SELECT id_plan, nombre, duracion_meses, precio
            FROM plan_membresia
            """;

    private static final RowMapper<PlanMembresia> FILA_PLAN = (rs, numeroFila) -> new PlanMembresia(
            rs.getLong("id_plan"),
            rs.getString("nombre"),
            rs.getInt("duracion_meses"),
            rs.getBigDecimal("precio"));

    private final JdbcTemplate jdbc;

    public PlanMembresiaDaoJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<PlanMembresia> listar() {
        return jdbc.query(SELECT_PLAN + "ORDER BY duracion_meses", FILA_PLAN);
    }

    @Override
    public Optional<PlanMembresia> obtenerPorId(Long idPlan) {
        return jdbc.query(SELECT_PLAN + "WHERE id_plan = ?", FILA_PLAN, idPlan).stream().findFirst();
    }
}
