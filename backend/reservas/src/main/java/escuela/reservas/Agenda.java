package escuela.reservas;

import java.security.Principal;
import java.time.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
class Agenda {
    record Entrada(UUID id, int espacio, LocalDate fecha, LocalTime inicio, LocalTime fin, int personas, String actividad) {}
    record Decision(String estado) {}
    record Reserva(UUID id, String usuario, int espacio, LocalDate fecha, LocalTime inicio, LocalTime fin,
                   int personas, String actividad, String estado) {}
    private final JdbcClient db;
    private static final int[] CAPACIDAD = {0,35,32,45,12,40,20,30};

    Agenda(JdbcClient db) { this.db = db; }

    @GetMapping("/health")
    Map<String,String> salud() { return Map.of("estado", "ok"); }

    @GetMapping("/reservas")
    List<Reserva> listar(Principal p) {
        return db.sql("""
            SELECT * FROM reserva_compartida WHERE usuario=? OR ?='inspectoria' OR estado='Aceptada'
            ORDER BY fecha, inicio, espacio, id
            """).params(p.getName(), p.getName()).query(Reserva.class).list();
    }

    @PostMapping("/reservas")
    @Transactional
    void crear(@RequestBody Entrada e, Principal p) {
        if (e.id()==null || e.espacio()<1 || e.espacio()>=CAPACIDAD.length || e.fecha()==null ||
            e.inicio()==null || e.fin()==null || e.actividad()==null || e.actividad().isBlank() ||
            e.actividad().length()>120 || e.personas()<1 || e.personas()>CAPACIDAD[e.espacio()])
            throw error("Revisa la actividad, el espacio y su capacidad.");
        bloquearEspacio(e.espacio());
        var existente = db.sql("SELECT * FROM reserva_compartida WHERE id=?").param(e.id()).query(Reserva.class).optional();
        if (existente.isPresent()) {
            var r=existente.get();
            if (r.usuario().equals(p.getName()) && r.espacio()==e.espacio() && r.fecha().equals(e.fecha()) &&
                r.inicio().equals(e.inicio()) && r.fin().equals(e.fin()) && r.personas()==e.personas() &&
                r.actividad().equals(e.actividad().trim())) return;
            throw conflicto("La solicitud ya existe con otros datos.");
        }
        validarHorario(e.fecha(), e.inicio(), e.fin());
        comprobarDisponible(e.espacio(),e.fecha(),e.inicio(),e.fin());
        db.sql("INSERT INTO reserva_compartida(id,usuario,espacio,fecha,inicio,fin,personas,actividad,estado) VALUES (?,?,?,?,?,?,?,?,?)")
            .params(e.id(),p.getName(),e.espacio(),e.fecha(),e.inicio(),e.fin(),e.personas(),e.actividad().trim(),
                p.getName().equals("inspectoria") ? "Aceptada" : "Pendiente").update();
    }

    @PutMapping("/reservas/{id}/decision")
    @Transactional
    void decidir(@PathVariable("id") UUID id, @RequestBody Decision d) {
        if (!"Aceptada".equals(d.estado()) && !"Rechazada".equals(d.estado())) throw error("Estado inválido.");
        int espacio=db.sql("SELECT espacio FROM reserva_compartida WHERE id=?").param(id).query(Integer.class)
            .optional().orElseThrow(() -> new Problema(HttpStatus.NOT_FOUND,"Solicitud inexistente."));
        bloquearEspacio(espacio);
        var r=db.sql("SELECT * FROM reserva_compartida WHERE id=?").param(id).query(Reserva.class).single();
        if (r.estado().equals(d.estado())) return;
        if (!r.estado().equals("Pendiente")) throw conflicto("La solicitud ya fue resuelta.");
        if (d.estado().equals("Aceptada")) {
            validarHorario(r.fecha(),r.inicio(),r.fin());
            comprobarDisponible(espacio,r.fecha(),r.inicio(),r.fin());
        }
        db.sql("UPDATE reserva_compartida SET estado=? WHERE id=?").params(d.estado(),id).update();
    }

    private void bloquearEspacio(int espacio) {
        db.sql("SELECT id FROM espacio_agenda WHERE id=? FOR UPDATE").param(espacio).query(Integer.class).single();
    }

    private void comprobarDisponible(int espacio, LocalDate fecha, LocalTime inicio, LocalTime fin) {
        if (db.sql("SELECT count(*) FROM reserva_compartida WHERE espacio=? AND fecha=? AND estado='Aceptada' AND inicio<? AND fin>?")
                .params(espacio,fecha,fin,inicio).query(Integer.class).single()>0)
            throw conflicto("Ese espacio ya está reservado en ese horario.");
    }

    private void validarHorario(LocalDate fecha, LocalTime inicio, LocalTime fin) {
        LocalDate hoy=LocalDate.now(ZoneId.of("America/Santiago"));
        if (!fecha.isAfter(hoy) || fecha.getDayOfWeek().getValue()>5) throw error("Elige un día hábil futuro.");
        LocalTime cierre=fecha.getDayOfWeek()==DayOfWeek.FRIDAY ? LocalTime.of(14,0) : LocalTime.of(15,30);
        if (!inicio.isBefore(fin) || inicio.isBefore(LocalTime.of(8,15)) || fin.isAfter(cierre) ||
            inicio.getSecond()!=0 || fin.getSecond()!=0 || inicio.getNano()!=0 || fin.getNano()!=0)
            throw error("El horario debe estar dentro de la jornada académica.");
    }

    private Problema error(String mensaje) { return new Problema(HttpStatus.BAD_REQUEST,mensaje); }
    private Problema conflicto(String mensaje) { return new Problema(HttpStatus.CONFLICT,mensaje); }
    private static class Problema extends RuntimeException {
        final HttpStatus estado;
        Problema(HttpStatus estado,String mensaje) { super(mensaje); this.estado=estado; }
    }
    @ExceptionHandler(Problema.class)
    ResponseEntity<Map<String,String>> responder(Problema e) {
        return ResponseEntity.status(e.estado).body(Map.of("mensaje",e.getMessage()));
    }
}
