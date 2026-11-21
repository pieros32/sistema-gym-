package com.ironlegacy.controlador;

import com.ironlegacy.servicio.NegocioException;
import com.ironlegacy.servicio.SocioServicio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/socios")
public class SocioControlador {

    private final SocioServicio socioServicio;

    public SocioControlador(SocioServicio socioServicio) {
        this.socioServicio = socioServicio;
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
}
