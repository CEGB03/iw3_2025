package ar.edu.iua.iw3.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CORS: https://developer.mozilla.org/es/docs/Web/HTTP/CORS
        // CSRF: https://developer.mozilla.org/es/docs/Glossary/CSRF
        http.cors(CorsConfigurer::disable);
        http.csrf(AbstractHttpConfigurer::disable);
        
        // Configurar sesiones stateless (JWT no usa sesiones)
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        
        http.authorizeHttpRequests(auth -> auth
            // Permitir acceso sin autenticación
            .requestMatchers("/api/v1/login").permitAll()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
            .requestMatchers("/actuator/**").permitAll()
            // Requerir autenticación para el resto
            .anyRequest().authenticated()
        );
        
        // Agregar filtro JWT antes del filtro de autenticación por defecto
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
