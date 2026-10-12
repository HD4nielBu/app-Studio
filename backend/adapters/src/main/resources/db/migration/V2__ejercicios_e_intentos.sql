-- Porción 2a · Ejercicio, intento y semáforo. El semáforo no se guarda: se calcula desde el historial.
create table ejercicios (
    id              uuid primary key,
    libro_id        uuid not null references libros (id) on delete cascade,
    concepto_id     uuid not null references conceptos (id), -- un concepto con ejercicios no se puede quitar
    enunciado       text not null,
    fuente          text,                                    -- null = ejercicio propio
    dificultad      text not null check (dificultad in ('BASICA', 'MEDIA', 'ALTA')),
    respuesta_final text not null,
    orden           bigint generated always as identity
);
create index on ejercicios (concepto_id);

create table intentos (
    id             uuid primary key,
    ejercicio_id   uuid not null references ejercicios (id) on delete cascade,
    concepto_id    uuid not null,
    estado         text not null check (estado in ('EN_CURSO', 'RESUELTO', 'RESCATADO')),
    pistas_usadas  int not null,
    pistas_maximas int not null,
    creado_en      timestamptz not null default now(),
    orden          bigint generated always as identity
);
create index on intentos (ejercicio_id);

-- Se guardan todas las respuestas, también las correctas. Solo se agregan, nunca se cambian.
create table respuestas (
    intento_id      uuid not null references intentos (id) on delete cascade,
    numero          int not null,
    desarrollo      text not null,
    respuesta_final text not null,
    confianza       int not null check (confianza between 1 and 3),
    correcta        boolean not null,
    tipo_fallo      text check (tipo_fallo in ('CONCEPTO', 'PROCEDIMIENTO', 'EJECUCION', 'LECTURA_DEL_ENUNCIADO', 'MEMORIA', 'PRERREQUISITO')),
    explicacion     text,
    creado_en       timestamptz not null default now(),
    primary key (intento_id, numero)
);
