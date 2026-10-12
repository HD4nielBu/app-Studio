-- Porción 1 · Libro y temario
create table libros (
    id           uuid primary key,
    nombre       text not null,
    materia      text not null,
    fecha_examen date,
    tono         text not null check (tono in ('ESTRICTO', 'NEUTRO', 'CERCANO')),
    tratamiento  text not null check (tratamiento in ('TU', 'VOS', 'USTED')),
    archivado    boolean not null default false,
    creado_en    timestamptz not null default now()
);

-- Un tema quitado del temario se conserva (en_temario = false) para no perder su historial.
create table temas (
    id         uuid primary key,
    libro_id   uuid not null references libros (id) on delete cascade,
    nombre     text not null,
    en_temario boolean not null default true,
    orden      bigint generated always as identity
);
create unique index temas_nombre_unico on temas (libro_id, lower(nombre));

create table conceptos (
    id       uuid primary key,
    tema_id  uuid not null references temas (id) on delete cascade,
    nombre   text not null,
    posicion int not null
);
create unique index conceptos_nombre_unico on conceptos (tema_id, lower(nombre));
