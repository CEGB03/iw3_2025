package ar.edu.iua.iw3.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.iua.iw3.controllers.config.JwtProperties;
import ar.edu.iua.iw3.model.business.exceptions.BusinessException;
import ar.edu.iua.iw3.model.business.exceptions.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import javax.crypto.SecretKey;

@Service
@Slf4j
public class JwtTokenProvider {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * Genera un token JWT para el usuario especificado
     */
    public String generateToken(String username) throws BusinessException {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
            
            long now = System.currentTimeMillis();
            Date issuedAt = new Date(now);
            Date expiresAt = new Date(now + jwtProperties.getExpirationTime());
            
            String token = Jwts.builder()
                    .subject(username)
                    .issuedAt(issuedAt)
                    .expiration(expiresAt)
                    .issuer(jwtProperties.getIssuer())
                    .audience().add(jwtProperties.getAudience()).and()
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();
            
            log.debug("Token generado para usuario: {}", username);
            return token;
            
        } catch (Exception e) {
            log.error("Error generando token JWT: {}", e.getMessage(), e);
            throw BusinessException.builder()
                    .ex(e)
                    .message("Error al generar token JWT")
                    .build();
        }
    }

    public String generateToken(String username, String role) throws BusinessException {
    try {
        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());

        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiresAt = new Date(now + jwtProperties.getExpirationTime());

        String token = Jwts.builder()
                .subject(username)
                .claim("role", role) // 🔥 ACÁ VIAJA EL ROL
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .issuer(jwtProperties.getIssuer())
                .audience().add(jwtProperties.getAudience()).and()
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        log.debug("Token generado para usuario: {} con rol {}", username, role);
        return token;

    } catch (Exception e) {
        log.error("Error generando token JWT: {}", e.getMessage(), e);
        throw BusinessException.builder()
                .ex(e)
                .message("Error al generar token JWT")
                .build();
    }
}

    /**
     * Valida un token JWT y retorna el nombre de usuario
     */
    public String validateAndGetUsername(String token) throws UnauthorizedException {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
            
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getPayload();
            
            String username = claims.getSubject();
            log.debug("Token válido para usuario: {}", username);
            return username;
            
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("Token expirado: {}", e.getMessage());
            throw UnauthorizedException.builder()
                    .message("El token ha expirado")
                    .build();
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.warn("Token malformado: {}", e.getMessage());
            throw UnauthorizedException.builder()
                    .message("Token inválido o malformado")
                    .build();
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            log.warn("Token no soportado: {}", e.getMessage());
            throw UnauthorizedException.builder()
                    .message("Tipo de token no soportado")
                    .build();
        } catch (IllegalArgumentException e) {
            log.warn("Token vacío: {}", e.getMessage());
            throw UnauthorizedException.builder()
                    .message("Token vacío o nulo")
                    .build();
        } catch (Exception e) {
            log.error("Error validando token: {}", e.getMessage(), e);
            throw UnauthorizedException.builder()
                    .message("Error al validar token")
                    .build();
        }
    }

    /**
     * Extrae el username de un token JWT sin validar firma
     */
    public String extractUsernameFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            log.error("Error extrayendo username del token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Verifica si un token está expirado
     */
    public boolean isTokenExpired(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getPayload();
            return claims.getExpiration().before(new Date());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    public String getRoleFromToken(String token) {
    try {
        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getPayload();

        return claims.get("role", String.class);

    } catch (Exception e) {
        log.error("Error extrayendo rol del token: {}", e.getMessage());
        return null;
    }
}

}