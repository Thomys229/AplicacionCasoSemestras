package escuela.materiales;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/materiales")
class Materiales {
    record Entrada(String nombre, String categoria) {}
    record Material(long id, String nombre, String categoria) {}

    private final JdbcTemplate db;
    Materiales(JdbcTemplate db) { this.db = db; }

    @GetMapping
    List<Material> listar() {
        return db.query("SELECT id, nombre, categoria FROM material ORDER BY id",
            (rs, fila) -> new Material(rs.getLong("id"), rs.getString("nombre"), rs.getString("categoria")));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    void agregar(@RequestBody Entrada entrada) {
        validar(entrada);
        db.update("INSERT INTO material(nombre, categoria) VALUES (?, ?)", entrada.nombre().trim(), entrada.categoria().trim());
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void editar(@PathVariable("id") long id, @RequestBody Entrada entrada) {
        validar(entrada);
        comprobar(db.update("UPDATE material SET nombre=?, categoria=? WHERE id=?", entrada.nombre().trim(), entrada.categoria().trim(), id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void eliminar(@PathVariable("id") long id) {
        comprobar(db.update("DELETE FROM material WHERE id=?", id));
    }

    private void comprobar(int filas) {
        if (filas == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    private void validar(Entrada e) {
        if (!textoValido(e.nombre(), 80) || !textoValido(e.categoria(), 40))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    private boolean textoValido(String texto, int max) {
        return texto != null && !texto.isBlank() && texto.trim().length() <= max;
    }
}
