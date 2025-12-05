package ar.edu.iua.iw3.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import ar.edu.iua.iw3.model.business.exceptions.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // Extraer token del header Authorization
            String token = extractTokenFromRequest(request);
            
            if (token != null) {
                try {
                    // Validar token y obtener username
                    String username = jwtTokenProvider.validateAndGetUsername(token);
                    
                    // Crear autenticación
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
                    
                    // Establecer en SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("Usuario {} autenticado con JWT", username);
                    
                } catch (UnauthorizedException e) {
                    log.warn("Token inválido: {}", e.getMessage());
                    SecurityContextHolder.clearContext();
                }
            }
            
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            log.error("Error en filtro JWT: {}", e.getMessage(), e);
            filterChain.doFilter(request, response);
        }
    }

    /**
     * Extrae el token JWT del header Authorization
     * Espera formato: "Bearer <token>"
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remover "Bearer "
        }
        
        return null;
    }
}