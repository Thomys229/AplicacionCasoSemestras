package escuela.reservas;

import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
class Seguridad {
    @Bean UserDetailsService usuarios(Environment config) {
        var encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        return new InMemoryUserDetailsManager(
            User.withUsername("docente").password(encoder.encode(config.getRequiredProperty("DOCENTE_PASSWORD"))).roles("DOCENTE").build(),
            User.withUsername("inspectoria").password(encoder.encode(config.getRequiredProperty("INSPECTORIA_PASSWORD"))).roles("ADMIN").build());
    }

    @Bean SecurityFilterChain acceso(HttpSecurity http) throws Exception {
        // Cliente Android sin cookies; se autentica en cada solicitud.
        return http.csrf(c -> c.disable()).sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a.requestMatchers("/api/health").permitAll()
                .requestMatchers("/api/reservas/*/decision").hasRole("ADMIN").anyRequest().authenticated())
            .httpBasic(withDefaults()).build();
    }
}
