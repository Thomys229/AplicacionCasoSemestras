CREATE TABLE IF NOT EXISTS espacio_agenda (id INTEGER PRIMARY KEY);
INSERT INTO espacio_agenda(id)
SELECT n FROM (VALUES (1),(2),(3),(4),(5),(6),(7)) AS ids(n)
WHERE NOT EXISTS (SELECT 1 FROM espacio_agenda WHERE id=n);
CREATE TABLE IF NOT EXISTS reserva_compartida (
    id UUID PRIMARY KEY,
    usuario VARCHAR(32) NOT NULL,
    espacio INTEGER NOT NULL REFERENCES espacio_agenda(id),
    fecha DATE NOT NULL,
    inicio TIME NOT NULL,
    fin TIME NOT NULL,
    personas INTEGER NOT NULL,
    actividad VARCHAR(120) NOT NULL,
    estado VARCHAR(16) NOT NULL
);
CREATE INDEX IF NOT EXISTS reserva_por_fecha ON reserva_compartida(espacio, fecha);
