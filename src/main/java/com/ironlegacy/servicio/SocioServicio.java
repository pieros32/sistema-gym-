package com.ironlegacy.servicio;

import com.ironlegacy.dao.SocioDao;
import com.ironlegacy.modelo.Socio;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SocioServicio {

    private final SocioDao socioDao;

    public SocioServicio(SocioDao socioDao) {
        this.socioDao = socioDao;
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
}
