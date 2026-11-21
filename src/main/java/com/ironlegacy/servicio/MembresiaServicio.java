package com.ironlegacy.servicio;

import com.ironlegacy.dao.MembresiaDao;
import com.ironlegacy.dao.PlanMembresiaDao;
import com.ironlegacy.dao.SocioDao;
import com.ironlegacy.modelo.MedioPago;
import com.ironlegacy.modelo.Membresia;
import com.ironlegacy.modelo.Pago;
import com.ironlegacy.modelo.PlanMembresia;
import com.ironlegacy.modelo.Socio;
import com.ironlegacy.modelo.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class MembresiaServicio {

    /** RN-07: ventana de aviso de vencimiento, en días. */
    static final int DIAS_POR_VENCER = 7;

    private final PlanMembresiaDao planMembresiaDao;
    private final MembresiaDao membresiaDao;
    private final SocioDao socioDao;
    private final PagoServicio pagoServicio;
    private final Clock reloj;

    public MembresiaServicio(PlanMembresiaDao planMembresiaDao, MembresiaDao membresiaDao, SocioDao socioDao,
                             PagoServicio pagoServicio, Clock reloj) {
        this.planMembresiaDao = planMembresiaDao;
        this.membresiaDao = membresiaDao;
        this.socioDao = socioDao;
        this.pagoServicio = pagoServicio;
        this.reloj = reloj;
    }

    public List<PlanMembresia> listarPlanes() {
        return planMembresiaDao.listar();
    }

    public PlanMembresia obtenerPlan(Long idPlan) {
        if (idPlan == null) {
            throw new NegocioException("Elige un plan.");
        }
        return planMembresiaDao.obtenerPorId(idPlan)
                .orElseThrow(() -> new NegocioException("El plan elegido no existe."));
    }

    /** Único punto de creación de membresías: el vencimiento sale del plan (RN-02). */
    public Membresia crear(Long idSocio, PlanMembresia plan, LocalDate inicio) {
        Membresia nueva = new Membresia(null, idSocio, plan.idPlan(), inicio, plan.calcularVencimiento(inicio));
        return membresiaDao.crear(nueva);
    }

    /** CU-03. La membresía y su pago se crean en una sola transacción. */
    @Transactional
    public ResultadoOperacion renovar(String dni, Long idPlan, MedioPago medioPago, Usuario usuario) {
        if (dni == null || !dni.trim().matches("\\d{8}")) {
            throw new NegocioException("El DNI debe tener 8 dígitos.");
        }
        Socio socio = socioDao.buscarPorDni(dni.trim())
                .orElseThrow(() -> new NegocioException("No existe un socio con ese DNI."));
        PlanMembresia plan = obtenerPlan(idPlan);

        LocalDate hoy = LocalDate.now(reloj);
        LocalDate inicio = inicioDeRenovacion(membresiaDao.obtenerUltima(socio), hoy);
        Membresia membresia = crear(socio.idSocio(), plan, inicio);
        Pago pago = pagoServicio.registrar(membresia, plan.precio(), medioPago, usuario, hoy);
        return new ResultadoOperacion(socio, membresia, pago);
    }

    /** CU-08 (RN-07): membresías que vencen de hoy a dentro de 7 días. */
    public List<VencimientoProximo> proximosAVencer() {
        LocalDate hoy = LocalDate.now(reloj);
        return membresiaDao.buscarPorVencer(hoy, hoy.plusDays(DIAS_POR_VENCER)).stream()
                .map(vencimiento -> new VencimientoProximo(
                        vencimiento.nombreCompleto(),
                        vencimiento.telefono(),
                        vencimiento.fechaVencimiento(),
                        (int) ChronoUnit.DAYS.between(hoy, vencimiento.fechaVencimiento())))
                .toList();
    }

    /** RN-04: si la membresía sigue vigente, la nueva empieza al día siguiente; si venció, hoy. */
    static LocalDate inicioDeRenovacion(Optional<Membresia> ultima, LocalDate hoy) {
        return ultima.filter(membresia -> membresia.esVigente(hoy))
                .map(membresia -> membresia.fechaVencimiento().plusDays(1))
                .orElse(hoy);
    }
}
