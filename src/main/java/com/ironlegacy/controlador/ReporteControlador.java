package com.ironlegacy.controlador;

import com.ironlegacy.servicio.NegocioException;
import com.ironlegacy.servicio.ReporteServicio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/reportes")
public class ReporteControlador {

    private final ReporteServicio reporteServicio;

    public ReporteControlador(ReporteServicio reporteServicio) {
        this.reporteServicio = reporteServicio;
    }

    @GetMapping
    public String reportes(@RequestParam(required = false) String mes, Model modelo) {
        try {
            modelo.addAttribute("resumen", reporteServicio.resumen(mes));
        } catch (NegocioException e) {
            modelo.addAttribute("error", e.getMessage());
        }
        return "reportes/index";
    }
}
