package com.ironlegacy.dao.jdbc;

import com.ironlegacy.dao.UsuarioDao;
import com.ironlegacy.modelo.Rol;
import com.ironlegacy.modelo.Usuario;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UsuarioDaoJdbc implements UsuarioDao {

    private static final String SQL_POR_NOMBRE = """
            SELECT id_usuario, nombre_completo, nombre_usuario, clave_hash, rol, activo
            FROM usuario
            WHERE nombre_usuario = ?
            """;

    private static final RowMapper<Usuario> FILA_USUARIO = (rs, numeroFila) -> new Usuario(
            rs.getLong("id_usuario"),
            rs.getString("nombre_completo"),
            rs.getString("nombre_usuario"),
            rs.getString("clave_hash"),
            Rol.valueOf(rs.getString("rol")),
            rs.getBoolean("activo"));

    private final JdbcTemplate jdbc;

    public UsuarioDaoJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario) {
        return jdbc.query(SQL_POR_NOMBRE, FILA_USUARIO, nombreUsuario).stream().findFirst();
    }
}
