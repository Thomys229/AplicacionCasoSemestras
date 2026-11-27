package escuela.catalogo;

import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
class Categorias {
    @GetMapping("/categorias")
    List<String> listar() {
        return List.of("Lectura", "Laboratorio", "Taller");
    }
}
