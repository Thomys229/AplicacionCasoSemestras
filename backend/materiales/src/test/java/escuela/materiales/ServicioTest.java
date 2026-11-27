package escuela.materiales;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:prueba")
@AutoConfigureMockMvc
class ServicioTest {
    @Autowired MockMvc api;

    @Test void modificaYEliminaElMaterial() throws Exception {
        String datos = "{\"nombre\":\"Atlas\",\"categoria\":\"Lectura\"}";
        api.perform(post("/materiales").contentType("application/json").content(datos)).andExpect(status().isCreated());
        api.perform(get("/materiales")).andExpect(jsonPath("$[0].nombre").value("Atlas"));
        api.perform(put("/materiales/1").contentType("application/json").content(datos.replace("Atlas", "Mapas")))
            .andExpect(status().isNoContent());
        api.perform(get("/materiales")).andExpect(jsonPath("$[0].nombre").value("Mapas"));
        api.perform(delete("/materiales/1")).andExpect(status().isNoContent());
        api.perform(get("/materiales")).andExpect(jsonPath("$").isEmpty());
        api.perform(delete("/materiales/1")).andExpect(status().isNotFound());
    }

    @Test void rechazaMaterialSinNombre() throws Exception {
        api.perform(post("/materiales").contentType("application/json").content("{}")).andExpect(status().isBadRequest());
    }
}
