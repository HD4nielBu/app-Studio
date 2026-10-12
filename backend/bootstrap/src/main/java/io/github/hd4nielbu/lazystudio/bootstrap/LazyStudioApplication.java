package io.github.hd4nielbu.lazystudio.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Escanea todo el proyecto para encontrar los adaptadores; el dominio y la aplicación no usan anotaciones de Spring.
@SpringBootApplication(scanBasePackages = "io.github.hd4nielbu.lazystudio")
public class LazyStudioApplication {

    public static void main(String[] args) {
        SpringApplication.run(LazyStudioApplication.class, args);
    }
}
