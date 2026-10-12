package io.github.hd4nielbu.lazystudio.application.ports.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Ejercicio;

public interface RepositorioDeEjercicios {

    void guardar(Ejercicio ejercicio);

    Optional<Ejercicio> buscarEjercicio(UUID id);

    List<Ejercicio> delConcepto(UUID conceptoId);
}
