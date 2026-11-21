package com.ironlegacy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Única fuente de la fecha y hora del sistema. Se inyecta un Clock para que las
 * reglas de vencimiento puedan probarse con fechas fijas.
 */
@Configuration
public class ConfiguracionFecha {

    public static final ZoneId ZONA_NEGOCIO = ZoneId.of("America/Lima");

    @Bean
    public Clock reloj() {
        return Clock.system(ZONA_NEGOCIO);
    }
}
