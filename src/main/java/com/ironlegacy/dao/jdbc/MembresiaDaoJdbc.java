package com.ironlegacy.dao.jdbc;

import com.ironlegacy.dao.MembresiaDao;
import com.ironlegacy.modelo.Membresia;
import com.ironlegacy.modelo.Socio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Repository
public class MembresiaDaoJdbc implements MembresiaDao {

    private static final String SQL_ULTIMA = """
            SELECT id_membresia, id_socio, id_plan, fecha_inicio, fecha_vencimiento
            FROM membresia
            WHERE id_socio = ?
            ORDER BY fecha_vencimiento DESC, id_membresia DESC
            LIMIT 1
            """;

    private static final RowMapper<Membresia> FILA_MEMBRESIA = (rs, numeroFila) -> new Membresia(
            rs.getLong("id_membresia"),
            rs.getLong("id_socio"),
            rs.getLong("id_plan"),
            rs.getObject("fecha_inicio", LocalDate.class),
            rs.getObject("fecha_vencimiento", LocalDate.class));

    private final JdbcTemplate jdbc;
    private final SimpleJdbcInsert insercion;

    public MembresiaDaoJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.insercion = new SimpleJdbcInsert(jdbc)
                .withTableName("membresia")
                .usingGeneratedKeyColumns("id_membresia");
    }

    @Override
    public Optional<Membresia> obtenerUltima(Socio socio) {
        return jdbc.query(SQL_ULTIMA, FILA_MEMBRESIA, socio.idSocio()).stream().findFirst();
    }

    @Override
    public Membresia crear(Membresia membresia) {
        Number idGenerado = insercion.executeAndReturnKey(Map.of(
                "id_socio", membresia.idSocio(),
                "id_plan", membresia.idPlan(),
                "fecha_inicio", membresia.fechaInicio(),
                "fecha_vencimiento", membresia.fechaVencimiento()));
        return new Membresia(idGenerado.longValue(), membresia.idSocio(), membresia.idPlan(),
                membresia.fechaInicio(), membresia.fechaVencimiento());
    }
}
