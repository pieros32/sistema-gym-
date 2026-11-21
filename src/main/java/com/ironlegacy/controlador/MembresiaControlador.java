package com.ironlegacy.controlador;

import com.ironlegacy.config.ClavesSesion;
import com.ironlegacy.modelo.MedioPago;
import com.ironlegacy.modelo.Usuario;
import com.ironlegacy.servicio.MembresiaServicio;
import com.ironlegacy.servicio.NegocioException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequestMapping("/membresias")
public class MembresiaControlador {

    private final MembresiaServicio membresiaServicio;

    public MembresiaControlador(MembresiaServicio membresiaServicio) {
        this.membresiaServicio = membresiaServicio;
    }

    @GetMapping("/renovar")
    public String formularioRenovacion(Model modelo) {
        cargarOpciones(modelo);
        return "membresias/renovar";
    }

    @PostMapping("/renovar")
    public String renovar(@RequestParam String dni,
                          @RequestParam(required = false) Long idPlan,
                          @RequestParam(required = false) MedioPago medioPago,
                          @SessionAttribute(ClavesSesion.USUARIO) Usuario usuario,
                          Model modelo) {
        modelo.addAttribute("dni", dni);
        try {
            modelo.addAttribute("operacion", membresiaServicio.renovar(dni, idPlan, medioPago, usuario));
        } catch (NegocioException e) {
            modelo.addAttribute("error", e.getMessage());
        }
        cargarOpciones(modelo);
        return "membresias/renovar";
    }

    private void cargarOpciones(Model modelo) {
        modelo.addAttribute("planes", membresiaServicio.listarPlanes());
        modelo.addAttribute("medios", MedioPago.values());
    }
}
