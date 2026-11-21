package com.ironlegacy.controlador;

import com.ironlegacy.servicio.NegocioException;
import com.ironlegacy.servicio.PagoServicio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/pagos")
public class PagoControlador {

    private final PagoServicio pagoServicio;

    public PagoControlador(PagoServicio pagoServicio) {
        this.pagoServicio = pagoServicio;
    }

    @GetMapping("/historial")
    public String historial(@RequestParam(required = false) String dni, Model modelo) {
        modelo.addAttribute("dni", dni == null ? "" : dni);
        if (dni != null) {
            try {
                modelo.addAttribute("historial", pagoServicio.historialPorDni(dni));
            } catch (NegocioException e) {
                modelo.addAttribute("error", e.getMessage());
            }
        }
        return "pagos/historial";
    }
}
