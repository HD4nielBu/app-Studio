package io.github.hd4nielbu.lazystudio.application.ports.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.github.hd4nielbu.lazystudio.domain.libro.Tema;

public interface RepositorioDeTemas {

    /** Guarda el tema y deja exactamente sus conceptos actuales. */
    void guardar(Tema tema);

    Optional<Tema> buscarTema(UUID id);

    Optional<Tema> temaDelConcepto(UUID conceptoId);

    /** En el orden en que se agregaron, incluidos los quitados del temario. */
    List<Tema> delLibro(UUID libroId);
}
