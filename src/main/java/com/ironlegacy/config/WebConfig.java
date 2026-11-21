package com.ironlegacy.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final InterceptorRoles interceptorRoles;

    public WebConfig(InterceptorRoles interceptorRoles) {
        this.interceptorRoles = interceptorRoles;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registro) {
        // /error no se intercepta: si no, un 403 se convertiría en una redirección a login.
        registro.addInterceptor(interceptorRoles)
                .excludePathPatterns("/login", "/css/**", "/error");
    }
}
