package escuela.reservas;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class AgendaTest {
    static final String clave = UUID.randomUUID().toString();
    @DynamicPropertySource static void configurar(DynamicPropertyRegistry p) {
        p.add("DB_URL", () -> System.getenv().getOrDefault("TEST_DB_URL","jdbc:h2:mem:agenda"));
        p.add("DB_USERNAME", () -> System.getenv().getOrDefault("TEST_DB_USER","sa"));
        p.add("DB_PASSWORD", () -> System.getenv().getOrDefault("TEST_DB_PASSWORD",""));
        p.add("DOCENTE_PASSWORD", () -> clave);
        p.add("INSPECTORIA_PASSWORD", () -> clave);
    }
    @Autowired TestRestTemplate cliente;
    TestRestTemplate docente() { return cliente.withBasicAuth("docente",clave); }
    TestRestTemplate admin() { return cliente.withBasicAuth("inspectoria",clave); }
    Map<String,Object> solicitud(int espacio) {
        return new HashMap<>(Map.of("id",UUID.randomUUID().toString(),"espacio",espacio,
            "fecha",LocalDate.now(ZoneId.of("America/Santiago")).with(TemporalAdjusters.next(DayOfWeek.MONDAY)).toString(),
            "inicio","09:00","fin","10:00","actividad","Prueba","personas",5));
    }
    ResponseEntity<String> decidir(TestRestTemplate usuario,Object id) {
        return usuario.exchange("/api/reservas/"+id+"/decision",HttpMethod.PUT,
            new HttpEntity<>(Map.of("estado","Aceptada")),String.class);
    }
    Agenda.Reserva leer(TestRestTemplate usuario,Object id) {
        return Arrays.stream(usuario.getForObject("/api/reservas",Agenda.Reserva[].class))
            .filter(r -> r.id().toString().equals(id)).findFirst().orElseThrow();
    }
    @Test void dosClientesVenLaMismaDecision() {
        var s=solicitud(1);
        assertEquals(401,cliente.getForEntity("/api/reservas",String.class).getStatusCode().value());
        assertTrue(docente().postForEntity("/api/reservas",s,String.class).getStatusCode().is2xxSuccessful());
        assertTrue(docente().postForEntity("/api/reservas",s,String.class).getStatusCode().is2xxSuccessful());
        assertEquals("Pendiente",leer(admin(),s.get("id")).estado());
        assertEquals(403,decidir(docente(),s.get("id")).getStatusCode().value());
        assertEquals(200,decidir(admin(),s.get("id")).getStatusCode().value());
        assertEquals("Aceptada",leer(docente(),s.get("id")).estado());
    }
    @Test void adminCreaUnaReservaVisibleAlDocente() {
        var s=solicitud(2);
        admin().postForEntity("/api/reservas",s,String.class);
        assertEquals("Aceptada",leer(docente(),s.get("id")).estado());
    }
    @Test void evitaDosAprobacionesSimultaneas() {
        var a=solicitud(3); var b=solicitud(3);
        docente().postForEntity("/api/reservas",a,String.class);
        docente().postForEntity("/api/reservas",b,String.class);
        var primera=CompletableFuture.supplyAsync(() -> decidir(admin(),a.get("id")).getStatusCode().value());
        var segunda=CompletableFuture.supplyAsync(() -> decidir(admin(),b.get("id")).getStatusCode().value());
        assertEquals(Set.of(200,409),Set.of(primera.join(),segunda.join()));
    }
    @Test void validaCapacidadEnServidor() {
        var s=solicitud(1); s.put("personas",36);
        assertEquals(400,docente().postForEntity("/api/reservas",s,String.class).getStatusCode().value());
    }
}
