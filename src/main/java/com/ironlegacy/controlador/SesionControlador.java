package com.ironlegacy.controlador;

import com.ironlegacy.config.ClavesSesion;
import com.ironlegacy.modelo.Usuario;
import com.ironlegacy.servicio.MembresiaServicio;
import com.ironlegacy.servicio.NegocioException;
import com.ironlegacy.servicio.SesionServicio;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
public class SesionControlador {

    private final SesionServicio sesionServicio;
    private final MembresiaServicio membresiaServicio;

    public SesionControlador(SesionServicio sesionServicio, MembresiaServicio membresiaServicio) {
        this.sesionServicio = sesionServicio;
        this.membresiaServicio = membresiaServicio;
    }

    @GetMapping("/")
    public String raiz() {
        return "redirect:/menu";
    }

    @GetMapping("/login")
    public String formularioLogin(HttpSession sesion) {
        return sesion.getAttribute(ClavesSesion.USUARIO) == null ? "login" : "redirect:/menu";
    }

    @PostMapping("/login")
    public String iniciarSesion(@RequestParam String nombreUsuario,
                                @RequestParam String clave,
                                HttpSession sesion,
                                Model modelo) {
        try {
            Usuario usuario = sesionServicio.autenticar(nombreUsuario, clave);
            sesion.setAttribute(ClavesSesion.USUARIO, usuario);
            return "redirect:/menu";
        } catch (NegocioException e) {
            modelo.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    @GetMapping("/menu")
    public String menu(@SessionAttribute(ClavesSesion.USUARIO) Usuario usuario, Model modelo) {
        modelo.addAttribute("usuario", usuario);
        modelo.addAttribute("porVencer", membresiaServicio.proximosAVencer().size());
        return "menu";
    }

    @GetMapping("/logout")
    public String cerrarSesion(HttpSession sesion) {
        sesion.invalidate();
        return "redirect:/login";
    }
}
