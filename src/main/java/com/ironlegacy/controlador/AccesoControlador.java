package com.ironlegacy.controlador;

import com.ironlegacy.config.ClavesSesion;
import com.ironlegacy.modelo.Usuario;
import com.ironlegacy.servicio.AccesoServicio;
import com.ironlegacy.servicio.NegocioException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequestMapping("/acceso")
public class AccesoControlador {

    private final AccesoServicio accesoServicio;

    public AccesoControlador(AccesoServicio accesoServicio) {
        this.accesoServicio = accesoServicio;
    }

    @GetMapping("/validar")
    public String formulario() {
        return "acceso/validar";
    }

    @PostMapping("/validar")
    public String validar(@RequestParam String dni,
                          @SessionAttribute(ClavesSesion.USUARIO) Usuario usuario,
                          Model modelo) {
        modelo.addAttribute("dni", dni);
        try {
            modelo.addAttribute("resultado", accesoServicio.validarAcceso(dni, usuario));
        } catch (NegocioException e) {
            modelo.addAttribute("error", e.getMessage());
        }
        return "acceso/validar";
    }
}
