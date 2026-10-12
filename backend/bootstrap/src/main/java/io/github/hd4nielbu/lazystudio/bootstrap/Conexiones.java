package io.github.hd4nielbu.lazystudio.bootstrap;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.hd4nielbu.lazystudio.application.ports.out.Corrector;
import io.github.hd4nielbu.lazystudio.application.ports.out.RepositorioDeEjercicios;
import io.github.hd4nielbu.lazystudio.application.ports.out.RepositorioDeIntentos;
import io.github.hd4nielbu.lazystudio.application.ports.out.RepositorioDeLibros;
import io.github.hd4nielbu.lazystudio.application.ports.out.RepositorioDeTemas;
import io.github.hd4nielbu.lazystudio.application.usecases.ServicioLibros;
import io.github.hd4nielbu.lazystudio.application.usecases.ServicioPractica;

/** Conecta los casos de uso (Java puro, sin anotaciones) con los adaptadores que Spring encuentra solo. */
@Configuration
class Conexiones {

    @Bean
    Clock reloj() {
        return Clock.systemDefaultZone();
    }

    @Bean
    ServicioLibros servicioLibros(RepositorioDeLibros libros, RepositorioDeTemas temas, Clock reloj) {
        return new ServicioLibros(libros, temas, reloj);
    }

    @Bean
    ServicioPractica servicioPractica(RepositorioDeTemas temas, RepositorioDeEjercicios ejercicios,
                                      RepositorioDeIntentos intentos, Corrector corrector) {
        return new ServicioPractica(temas, ejercicios, intentos, corrector);
    }
}
