package com.ironlegacy.servicio;

import com.ironlegacy.dao.SocioDao;
import com.ironlegacy.modelo.Membresia;
import com.ironlegacy.modelo.Pago;
import com.ironlegacy.modelo.PlanMembresia;
import com.ironlegacy.modelo.Socio;
import com.ironlegacy.modelo.Usuario;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
public class SocioServicio {

    private static final String MENSAJE_DNI_EXISTENTE =
            "Ya existe un socio con ese DNI. Si quiere renovar su membresía, use Renovar membresía.";

    private final SocioDao socioDao;
    private final MembresiaServicio membresiaServicio;
    private final PagoServicio pagoServicio;
    private final Clock reloj;

    public SocioServicio(SocioDao socioDao, MembresiaServicio membresiaServicio,
                         PagoServicio pagoServicio, Clock reloj) {
        this.socioDao = socioDao;
        this.membresiaServicio = membresiaServicio;
        this.pagoServicio = pagoServicio;
        this.reloj = reloj;
    }

    /** CU-05: un criterio de solo dígitos busca por DNI exacto; cualquier otro, por nombre parcial. */
    public List<Socio> buscar(String criterio) {
        String texto = criterio == null ? "" : criterio.trim();
        if (texto.isEmpty()) {
            throw new NegocioException("Escribe un DNI o un nombre para buscar.");
        }
        if (texto.matches("\\d+")) {
            if (texto.length() != 8) {
                throw new NegocioException("El DNI debe tener 8 dígitos.");
            }
            return socioDao.buscarPorDni(texto).map(List::of).orElse(List.of());
        }
        return socioDao.buscarPorNombre(texto);
    }

    /**
     * CU-02. Socio, membresía que empieza hoy y pago se crean en una sola transacción:
     * si el pago falla, no queda ningún registro.
     */
    @Transactional
    public ResultadoOperacion registrar(DatosRegistroSocio datos, Usuario recepcionista) {
        validar(datos);
        if (socioDao.existePorDni(datos.dni())) {
            throw new NegocioException(MENSAJE_DNI_EXISTENTE);
        }
        PlanMembresia plan = membresiaServicio.obtenerPlan(datos.idPlan());

        LocalDate hoy = LocalDate.now(reloj);
        Socio socio = crearSocio(datos, hoy);
        Membresia membresia = membresiaServicio.crear(socio.idSocio(), plan, hoy);
        Pago pago = pagoServicio.registrar(membresia, plan.precio(), datos.medioPago(), recepcionista, hoy);
        return new ResultadoOperacion(socio, membresia, pago);
    }

    private Socio crearSocio(DatosRegistroSocio datos, LocalDate hoy) {
        try {
            return socioDao.crear(new Socio(null, datos.nombreCompleto().trim(), datos.dni(), datos.telefono(), hoy));
        } catch (DuplicateKeyException e) {
            // Dos recepcionistas pueden registrar el mismo DNI a la vez: la BD es la última barrera (RN-01).
            throw new NegocioException(MENSAJE_DNI_EXISTENTE);
        }
    }

    private static void validar(DatosRegistroSocio datos) {
        if (datos.nombreCompleto() == null || datos.nombreCompleto().isBlank()) {
            throw new NegocioException("Escribe el nombre del socio.");
        }
        if (datos.nombreCompleto().trim().length() > 100) {
            throw new NegocioException("El nombre no puede tener más de 100 caracteres.");
        }
        if (datos.dni() == null || !datos.dni().matches("\\d{8}")) {
            throw new NegocioException("El DNI debe tener 8 dígitos.");
        }
        if (datos.telefono() == null || !datos.telefono().matches("\\d{9}")) {
            throw new NegocioException("El teléfono debe tener 9 dígitos.");
        }
        if (datos.medioPago() == null) {
            throw new NegocioException("Elige un medio de pago.");
        }
    }
}
