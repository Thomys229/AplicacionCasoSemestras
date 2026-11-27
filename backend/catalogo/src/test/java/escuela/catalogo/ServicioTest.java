package escuela.catalogo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ServicioTest {
    @Autowired MockMvc api;

    @Test void listaCategorias() throws Exception {
        api.perform(get("/categorias")).andExpect(status().isOk()).andExpect(jsonPath("$[0]").value("Lectura"));
    }
}
