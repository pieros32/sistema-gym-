package com.ironlegacy.dao.jdbc;

import com.ironlegacy.dao.PagoDao;
import com.ironlegacy.modelo.MedioPago;
import com.ironlegacy.modelo.Pago;
import com.ironlegacy.modelo.PagoDetalle;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public class PagoDaoJdbc implements PagoDao {

    private static final String SQL_HISTORIAL = """
            SELECT pg.fecha_pago, pg.monto, pg.medio_pago, p.nombre AS nombre_plan,
                   m.fecha_inicio, m.fecha_vencimiento, u.nombre_completo AS nombre_usuario
            FROM pago pg
            JOIN membresia m ON m.id_membresia = pg.id_membresia
            JOIN plan_membresia p ON p.id_plan = m.id_plan
            JOIN usuario u ON u.id_usuario = pg.id_usuario
            WHERE m.id_socio = ?
            ORDER BY pg.fecha_pago DESC, pg.id_pago DESC
            """;

    private static final RowMapper<PagoDetalle> FILA_DETALLE = (rs, numeroFila) -> new PagoDetalle(
            rs.getObject("fecha_pago", LocalDate.class),
            rs.getBigDecimal("monto"),
            MedioPago.valueOf(rs.getString("medio_pago")),
            rs.getString("nombre_plan"),
            rs.getObject("fecha_inicio", LocalDate.class),
            rs.getObject("fecha_vencimiento", LocalDate.class),
            rs.getString("nombre_usuario"));

    private final JdbcTemplate jdbc;
    private final SimpleJdbcInsert insercion;

    public PagoDaoJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.insercion = new SimpleJdbcInsert(jdbc)
                .withTableName("pago")
                .usingGeneratedKeyColumns("id_pago");
    }

    @Override
    public Pago crear(Pago pago) {
        Number idGenerado = insercion.executeAndReturnKey(Map.of(
                "id_membresia", pago.idMembresia(),
                "id_usuario", pago.idUsuario(),
                "monto", pago.monto(),
                "fecha_pago", pago.fechaPago(),
                "medio_pago", pago.medioPago().name()));
        return new Pago(idGenerado.longValue(), pago.idMembresia(), pago.idUsuario(),
                pago.monto(), pago.fechaPago(), pago.medioPago());
    }

    @Override
    public List<PagoDetalle> listarPorSocio(Long idSocio) {
        return jdbc.query(SQL_HISTORIAL, FILA_DETALLE, idSocio);
    }

    @Override
    public BigDecimal sumarIngresos(LocalDate desde, LocalDate hasta) {
        return jdbc.queryForObject(
                "SELECT COALESCE(SUM(monto), 0) FROM pago WHERE fecha_pago BETWEEN ? AND ?",
                BigDecimal.class, desde, hasta);
    }
}
