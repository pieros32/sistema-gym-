package com.ironlegacy.dao.jdbc;

import com.ironlegacy.dao.PagoDao;
import com.ironlegacy.modelo.Pago;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class PagoDaoJdbc implements PagoDao {

    private final SimpleJdbcInsert insercion;

    public PagoDaoJdbc(JdbcTemplate jdbc) {
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
}
