package com.ironlegacy.servicio;

import com.ironlegacy.dao.PlanMembresiaDao;
import com.ironlegacy.modelo.PlanMembresia;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MembresiaServicio {

    private final PlanMembresiaDao planMembresiaDao;

    public MembresiaServicio(PlanMembresiaDao planMembresiaDao) {
        this.planMembresiaDao = planMembresiaDao;
    }

    public List<PlanMembresia> listarPlanes() {
        return planMembresiaDao.listar();
    }
}
