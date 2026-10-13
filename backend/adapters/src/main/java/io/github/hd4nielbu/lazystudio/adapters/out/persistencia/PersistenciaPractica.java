package io.github.hd4nielbu.lazystudio.adapters.out.persistencia;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import io.github.hd4nielbu.lazystudio.application.ports.out.RepositorioDeEjercicios;
import io.github.hd4nielbu.lazystudio.application.ports.out.RepositorioDeIntentos;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Correccion;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Dificultad;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Ejercicio;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Intento;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Respuesta;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.TipoDeFallo;

@Repository
class PersistenciaPractica implements RepositorioDeEjercicios, RepositorioDeIntentos {

    private static final String EJERCICIO =
            "select id, libro_id, concepto_id, enunciado, fuente, dificultad, respuesta_final from ejercicios";

    private final JdbcClient jdbc;

    PersistenciaPractica(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void guardar(Ejercicio e) {
        jdbc.sql("""
                insert into ejercicios (id, libro_id, concepto_id, enunciado, fuente, dificultad, respuesta_final)
                values (:id, :libro, :concepto, :enunciado, :fuente, :dificultad, :respuesta)""")
                .param("id", e.id()).param("libro", e.libroId()).param("concepto", e.conceptoId())
                .param("enunciado", e.enunciado()).param("fuente", e.fuente())
                .param("dificultad", e.dificultad().name()).param("respuesta", e.respuestaFinal())
                .update();
    }

    @Override
    public Optional<Ejercicio> buscarEjercicio(UUID id) {
        return jdbc.sql(EJERCICIO + " where id = ?").param(id).query(this::ejercicio).optional();
    }

    @Override
    public List<Ejercicio> delConcepto(UUID conceptoId) {
        return jdbc.sql(EJERCICIO + " where concepto_id = ? order by orden").param(conceptoId).query(this::ejercicio).list();
    }

    @Override
    @Transactional
    public void guardar(Intento i) {
        jdbc.sql("""
                insert into intentos (id, ejercicio_id, concepto_id, estado, pistas_usadas, pistas_maximas)
                values (:id, :ejercicio, :concepto, :estado, :usadas, :maximas)
                on conflict (id) do update set estado = excluded.estado, pistas_usadas = excluded.pistas_usadas""")
                .param("id", i.id()).param("ejercicio", i.ejercicioId()).param("concepto", i.conceptoId())
                .param("estado", i.estado().name()).param("usadas", i.pistasUsadas()).param("maximas", i.pistasMaximas())
                .update();
        for (int n = 0; n < i.respuestas().size(); n++) {
            var r = i.respuestas().get(n);
            var c = r.correccion();
            jdbc.sql("""
                    insert into respuestas (intento_id, numero, desarrollo, respuesta_final, confianza, correcta, tipo_fallo, explicacion, linea_error)
                    values (:intento, :numero, :desarrollo, :final, :confianza, :correcta, :tipo, :explicacion, :linea)
                    on conflict do nothing""")
                    .param("intento", i.id()).param("numero", n).param("desarrollo", r.desarrollo())
                    .param("final", r.respuestaFinal()).param("confianza", r.confianza()).param("correcta", c.correcta())
                    .param("tipo", c.tipoDeFallo() == null ? null : c.tipoDeFallo().name()).param("explicacion", c.explicacion())
                    .param("linea", c.lineaDelError())
                    .update();
        }
    }

    @Override
    public Optional<Intento> buscarIntento(UUID id) {
        return intentos("i.id = ?", id).stream().findFirst();
    }

    @Override
    public Optional<Intento> enCurso(UUID ejercicioId) {
        return intentos("i.ejercicio_id = ? and i.estado = 'EN_CURSO'", ejercicioId).stream().findFirst();
    }

    @Override
    public List<Intento> intentosDelLibro(UUID libroId) {
        return intentos("i.ejercicio_id in (select id from ejercicios where libro_id = ?)", libroId);
    }

    // filtro es siempre una constante de esta clase, nunca un dato del usuario.
    private List<Intento> intentos(String filtro, UUID valor) {
        Map<UUID, List<Respuesta>> respuestas = jdbc.sql("""
                select r.* from respuestas r join intentos i on i.id = r.intento_id
                where %s order by r.numero""".formatted(filtro))
                .param(valor)
                .query((rs, n) -> Map.entry(rs.getObject("intento_id", UUID.class), respuesta(rs)))
                .list().stream()
                .collect(groupingBy(Map.Entry::getKey, mapping(Map.Entry::getValue, toList())));

        return jdbc.sql("select * from intentos i where %s order by i.orden".formatted(filtro))
                .param(valor)
                .query((rs, n) -> {
                    var id = rs.getObject("id", UUID.class);
                    return new Intento(id, rs.getObject("ejercicio_id", UUID.class), rs.getObject("concepto_id", UUID.class),
                            Intento.Estado.valueOf(rs.getString("estado")), rs.getInt("pistas_usadas"),
                            rs.getInt("pistas_maximas"), respuestas.getOrDefault(id, List.of()));
                })
                .list();
    }

    private static Respuesta respuesta(ResultSet rs) throws SQLException {
        var tipo = rs.getString("tipo_fallo");
        return new Respuesta(rs.getString("desarrollo"), rs.getString("respuesta_final"), rs.getInt("confianza"),
                new Correccion(rs.getBoolean("correcta"), tipo == null ? null : TipoDeFallo.valueOf(tipo), rs.getString("explicacion"),
                rs.getObject("linea_error", Integer.class)));
    }

    private Ejercicio ejercicio(ResultSet rs, int n) throws SQLException {
        return new Ejercicio(rs.getObject("id", UUID.class), rs.getObject("libro_id", UUID.class),
                rs.getObject("concepto_id", UUID.class), rs.getString("enunciado"), rs.getString("fuente"),
                Dificultad.valueOf(rs.getString("dificultad")), rs.getString("respuesta_final"));
    }
}
