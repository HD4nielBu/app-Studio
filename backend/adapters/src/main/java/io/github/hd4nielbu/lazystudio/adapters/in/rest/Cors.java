package io.github.hd4nielbu.lazystudio.adapters.in.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** La API solo acepta pedidos de la app (mismos orígenes que /api/salud). */
@Configuration
class Cors implements WebMvcConfigurer {

    private final String[] origenes;

    Cors(@Value("${lazystudio.origenes}") String[] origenes) {
        this.origenes = origenes;
    }

    @Override
    public void addCorsMappings(CorsRegistry registro) {
        registro.addMapping("/api/**").allowedOrigins(origenes).allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}
