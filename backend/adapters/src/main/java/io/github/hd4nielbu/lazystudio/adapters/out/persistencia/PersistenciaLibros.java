package io.github.hd4nielbu.lazystudio.adapters.out.persistencia;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import io.github.hd4nielbu.lazystudio.application.ports.out.RepositorioDeLibros;
import io.github.hd4nielbu.lazystudio.application.ports.out.RepositorioDeTemas;
import io.github.hd4nielbu.lazystudio.domain.ReglaVioladaException;
import io.github.hd4nielbu.lazystudio.domain.libro.Concepto;
import io.github.hd4nielbu.lazystudio.domain.libro.Libro;
import io.github.hd4nielbu.lazystudio.domain.libro.Tema;
import io.github.hd4nielbu.lazystudio.domain.libro.Tono;
import io.github.hd4nielbu.lazystudio.domain.libro.Tratamiento;

@Repository
class PersistenciaLibros implements RepositorioDeLibros, RepositorioDeTemas {

    private static final String LIBRO = "select id, nombre, materia, fecha_examen, tono, tratamiento, archivado from libros";

    private final JdbcClient jdbc;

    PersistenciaLibros(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void guardar(Libro l) {
        jdbc.sql("""
                insert into libros (id, nombre, materia, fecha_examen, tono, tratamiento, archivado)
                values (:id, :nombre, :materia, :fecha, :tono, :tratamiento, :archivado)
                on conflict (id) do update set nombre = excluded.nombre, materia = excluded.materia,
                    fecha_examen = excluded.fecha_examen, tono = excluded.tono,
                    tratamiento = excluded.tratamiento, archivado = excluded.archivado""")
                .param("id", l.id()).param("nombre", l.nombre()).param("materia", l.materia())
                .param("fecha", l.fechaExamen()).param("tono", l.tono().name())
                .param("tratamiento", l.tratamiento().name()).param("archivado", l.archivado())
                .update();
    }

    @Override
    public Optional<Libro> buscar(UUID id) {
        return jdbc.sql(LIBRO + " where id = ?").param(id).query(this::libro).optional();
    }

    @Override
    public List<Libro> todos() {
        return jdbc.sql(LIBRO + " order by creado_en").query(this::libro).list();
    }

    @Override
    public void eliminar(UUID id) {
        jdbc.sql("delete from libros where id = ?").param(id).update(); // temas y conceptos caen en cascada
    }

    @Override
    @Transactional
    public void guardar(Tema t) {
        jdbc.sql("""
                insert into temas (id, libro_id, nombre, en_temario) values (:id, :libro, :nombre, :enTemario)
                on conflict (id) do update set nombre = excluded.nombre, en_temario = excluded.en_temario""")
                .param("id", t.id()).param("libro", t.libroId()).param("nombre", t.nombre())
                .param("enTemario", t.enTemario()).update();

        var ids = t.conceptos().stream().map(Concepto::id).toList();
        try {
            if (ids.isEmpty()) {
                jdbc.sql("delete from conceptos where tema_id = ?").param(t.id()).update();
            } else {
                jdbc.sql("delete from conceptos where tema_id = :tema and id not in (:ids)")
                        .param("tema", t.id()).param("ids", ids).update();
            }
        } catch (DataIntegrityViolationException e) {
            throw new ReglaVioladaException("Un concepto que ya tiene ejercicios no se puede quitar de la división.");
        }
        for (int i = 0; i < t.conceptos().size(); i++) {
            var c = t.conceptos().get(i);
            jdbc.sql("""
                    insert into conceptos (id, tema_id, nombre, posicion) values (:id, :tema, :nombre, :pos)
                    on conflict (id) do update set nombre = excluded.nombre, posicion = excluded.posicion""")
                    .param("id", c.id()).param("tema", t.id()).param("nombre", c.nombre()).param("pos", i).update();
        }
    }

    @Override
    public Optional<Tema> buscarTema(UUID id) {
        return temas("t.id = ?", id).stream().findFirst();
    }

    @Override
    public Optional<Tema> temaDelConcepto(UUID conceptoId) {
        return temas("t.id = (select tema_id from conceptos where id = ?)", conceptoId).stream().findFirst();
    }

    @Override
    public List<Tema> delLibro(UUID libroId) {
        return temas("t.libro_id = ?", libroId);
    }

    // filtro es siempre una constante de esta clase, nunca un dato del usuario.
    private List<Tema> temas(String filtro, UUID valor) {
        Map<UUID, List<Concepto>> conceptos = jdbc.sql("""
                select c.tema_id, c.id, c.nombre from conceptos c join temas t on t.id = c.tema_id
                where %s order by c.posicion""".formatted(filtro))
                .param(valor)
                .query((rs, n) -> Map.entry(rs.getObject("tema_id", UUID.class),
                        new Concepto(rs.getObject("id", UUID.class), rs.getString("nombre"))))
                .list().stream()
                .collect(groupingBy(Map.Entry::getKey, mapping(Map.Entry::getValue, toList())));

        return jdbc.sql("select id, libro_id, nombre, en_temario from temas t where %s order by orden".formatted(filtro))
                .param(valor)
                .query((rs, n) -> {
                    var id = rs.getObject("id", UUID.class);
                    return new Tema(id, rs.getObject("libro_id", UUID.class), rs.getString("nombre"),
                            rs.getBoolean("en_temario"), conceptos.getOrDefault(id, List.of()));
                })
                .list();
    }

    private Libro libro(ResultSet rs, int n) throws SQLException {
        return new Libro(rs.getObject("id", UUID.class), rs.getString("nombre"), rs.getString("materia"),
                rs.getObject("fecha_examen", LocalDate.class), Tono.valueOf(rs.getString("tono")),
                Tratamiento.valueOf(rs.getString("tratamiento")), rs.getBoolean("archivado"));
    }
}
