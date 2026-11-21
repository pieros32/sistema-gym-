package com.ironlegacy.dao.jdbc;

import com.ironlegacy.dao.MembresiaDao;
import com.ironlegacy.modelo.Membresia;
import com.ironlegacy.modelo.Socio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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

    public MembresiaDaoJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Membresia> obtenerUltima(Socio socio) {
        return jdbc.query(SQL_ULTIMA, FILA_MEMBRESIA, socio.idSocio()).stream().findFirst();
    }
}
