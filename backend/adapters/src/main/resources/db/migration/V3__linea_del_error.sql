-- Porción 3 · La IA dice en qué línea del desarrollo está el primer error (R4). Null = no se sabe.
alter table respuestas add column linea_error int check (linea_error >= 1);
