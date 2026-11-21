package com.ironlegacy.dao.jdbc;

import com.ironlegacy.dao.AsistenciaDao;
import com.ironlegacy.modelo.Asistencia;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AsistenciaDaoJdbc implements AsistenciaDao {

    private static final String SQL_REGISTRAR = """
            INSERT INTO asistencia (id_socio, id_usuario, fecha_hora, resultado, motivo_denegacion)
            VALUES (?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbc;

    public AsistenciaDaoJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void registrar(Asistencia asistencia) {
        jdbc.update(SQL_REGISTRAR,
                asistencia.idSocio(),
                asistencia.idUsuario(),
                asistencia.fechaHora(),
                asistencia.resultado().name(),
                asistencia.motivoDenegacion());
    }
}
