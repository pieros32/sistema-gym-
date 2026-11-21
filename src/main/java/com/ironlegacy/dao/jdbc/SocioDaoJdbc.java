package com.ironlegacy.dao.jdbc;

import com.ironlegacy.dao.SocioDao;
import com.ironlegacy.modelo.Socio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
    private final SimpleJdbcInsert insercion;

    public SocioDaoJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.insercion = new SimpleJdbcInsert(jdbc)
                .withTableName("socio")
                .usingGeneratedKeyColumns("id_socio");
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

    @Override
    public boolean existePorDni(String dni) {
        Integer coincidencias = jdbc.queryForObject("SELECT COUNT(*) FROM socio WHERE dni = ?", Integer.class, dni);
        return coincidencias != null && coincidencias > 0;
    }

    @Override
    public long contar() {
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM socio", Long.class);
        return total == null ? 0 : total;
    }

    @Override
    public Socio crear(Socio socio) {
        Number idGenerado = insercion.executeAndReturnKey(Map.of(
                "nombre_completo", socio.nombreCompleto(),
                "dni", socio.dni(),
                "telefono", socio.telefono(),
                "fecha_registro", socio.fechaRegistro()));
        return new Socio(idGenerado.longValue(), socio.nombreCompleto(), socio.dni(),
                socio.telefono(), socio.fechaRegistro());
    }
}
