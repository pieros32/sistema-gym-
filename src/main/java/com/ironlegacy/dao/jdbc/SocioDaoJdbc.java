package com.ironlegacy.dao.jdbc;

import com.ironlegacy.dao.SocioDao;
import com.ironlegacy.modelo.Socio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class SocioDaoJdbc implements SocioDao {

    private static final String SELECT_SOCIO = """
            SELECT id_socio, nombre_completo, dni, telefono, fecha_registro
            FROM socio
            """;

    private static final RowMapper<Socio> FILA_SOCIO = (rs, numeroFila) -> new Socio(
            rs.getLong("id_socio"),
            rs.getString("nombre_completo"),
            rs.getString("dni"),
            rs.getString("telefono"),
            rs.getObject("fecha_registro", LocalDate.class));

    private final JdbcTemplate jdbc;

    public SocioDaoJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Socio> buscarPorDni(String dni) {
        return jdbc.query(SELECT_SOCIO + "WHERE dni = ?", FILA_SOCIO, dni).stream().findFirst();
    }

    @Override
    public List<Socio> buscarPorNombre(String fragmento) {
        // Escapa los comodines de LIKE para que el texto del usuario se busque literal.
        String patron = "%" + fragmento.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
        return jdbc.query(SELECT_SOCIO + "WHERE nombre_completo LIKE ? ORDER BY nombre_completo", FILA_SOCIO, patron);
    }
}
