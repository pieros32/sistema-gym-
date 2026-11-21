package com.ironlegacy.controlador;

import com.ironlegacy.config.ClavesSesion;
import com.ironlegacy.modelo.MedioPago;
import com.ironlegacy.modelo.Usuario;
import com.ironlegacy.servicio.DatosRegistroSocio;
import com.ironlegacy.servicio.MembresiaServicio;
import com.ironlegacy.servicio.NegocioException;
import com.ironlegacy.servicio.SocioServicio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequestMapping("/socios")
public class SocioControlador {

    private final SocioServicio socioServicio;
    private final MembresiaServicio membresiaServicio;

    public SocioControlador(SocioServicio socioServicio, MembresiaServicio membresiaServicio) {
        this.socioServicio = socioServicio;
        this.membresiaServicio = membresiaServicio;
    }

    @GetMapping("/buscar")
    public String buscar(@RequestParam(required = false) String texto, Model modelo) {
        modelo.addAttribute("texto", texto == null ? "" : texto);
        if (texto != null) {
            try {
                modelo.addAttribute("resultados", socioServicio.buscar(texto));
            } catch (NegocioException e) {
                modelo.addAttribute("error", e.getMessage());
            }
        }
        return "socios/buscar";
    }

    @GetMapping("/registro")
    public String formularioRegistro(Model modelo) {
        cargarOpciones(modelo);
        return "socios/registro";
    }

    @PostMapping("/registro")
    public String registrar(@RequestParam String nombreCompleto,
                            @RequestParam String dni,
                            @RequestParam String telefono,
                            @RequestParam(required = false) Long idPlan,
                            @RequestParam(required = false) MedioPago medioPago,
                            @SessionAttribute(ClavesSesion.USUARIO) Usuario usuario,
                            Model modelo) {
        modelo.addAttribute("nombreCompleto", nombreCompleto);
        modelo.addAttribute("dni", dni);
        modelo.addAttribute("telefono", telefono);
        try {
            DatosRegistroSocio datos = new DatosRegistroSocio(nombreCompleto, dni, telefono, idPlan, medioPago);
            modelo.addAttribute("operacion", socioServicio.registrar(datos, usuario));
        } catch (NegocioException e) {
            modelo.addAttribute("error", e.getMessage());
        }
        cargarOpciones(modelo);
        return "socios/registro";
    }

    private void cargarOpciones(Model modelo) {
        modelo.addAttribute("planes", membresiaServicio.listarPlanes());
        modelo.addAttribute("medios", MedioPago.values());
    }
}
