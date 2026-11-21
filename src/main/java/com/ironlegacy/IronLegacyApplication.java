package com.ironlegacy;

import com.ironlegacy.servicio.MembresiaServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class IronLegacyApplication {

    private static final Logger log = LoggerFactory.getLogger(IronLegacyApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(IronLegacyApplication.class, args);
    }

    // Comprobación de arranque de E1: confirma la conexión a MySQL leyendo los planes.
    @Bean
    CommandLineRunner comprobarConexionPlanes(MembresiaServicio membresiaServicio) {
        return args -> membresiaServicio.listarPlanes().forEach(plan ->
                log.info("Plan cargado desde MySQL: {} ({} mes(es), {})",
                        plan.nombre(), plan.duracionMeses(), plan.precio()));
    }
}
