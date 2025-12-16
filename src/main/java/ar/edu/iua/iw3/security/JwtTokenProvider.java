package ar.edu.iua.iw3.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.iua.iw3.controllers.config.JwtProperties;
import ar.edu.iua.iw3.model.business.exceptions.BusinessException;
import ar.edu.iua.iw3.model.business.exceptions.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
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
            SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
            long now = System.currentTimeMillis();
            Date issuedAt = new Date(now);
            Date expiresAt = new Date(now + jwtProperties.getExpirationTime());

            String token = Jwts.builder()
                    .subject(username)
                    .issuedAt(issuedAt)
                    .expiration(expiresAt)
                    .issuer(jwtProperties.getIssuer())
                    .audience().add(jwtProperties.getAudience()).and()
                    .signWith(key, Jwts.SIG.HS256)
                    .compact();

            log.debug("Token generado para usuario: {}", username);
            return token;
        } catch (Exception e) {
            log.error("Error generando token JWT: {}", e.getMessage(), e);
            throw BusinessException.builder().ex(e).message("Error al generar token JWT").build();
        }
    }

    public String generateToken(String username, String role) throws BusinessException {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
            long now = System.currentTimeMillis();
            Date issuedAt = new Date(now);
            Date expiresAt = new Date(now + jwtProperties.getExpirationTime());

            String token = Jwts.builder()
                    .subject(username)
                    .claim("role", role)
                    .issuedAt(issuedAt)
                    .expiration(expiresAt)
                    .issuer(jwtProperties.getIssuer())
                    .audience().add(jwtProperties.getAudience()).and()
                    .signWith(key, Jwts.SIG.HS256)
                    .compact();

            log.debug("Token generado para usuario: {} con rol {}", username, role);
            return token;
        } catch (Exception e) {
            log.error("Error generando token JWT: {}", e.getMessage(), e);
            throw BusinessException.builder().ex(e).message("Error al generar token JWT").build();
        }
    }

    /**
     * Valida un token JWT y retorna el nombre de usuario
     */
    public String validateAndGetUsername(String token) throws UnauthorizedException {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));

            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw UnauthorizedException.builder().message("El token ha expirado").build();
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            throw UnauthorizedException.builder().message("Token inválido o malformado").build();
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            throw UnauthorizedException.builder().message("Tipo de token no soportado").build();
        } catch (IllegalArgumentException e) {
            throw UnauthorizedException.builder().message("Token vacío o nulo").build();
        } catch (Exception e) {
            throw UnauthorizedException.builder().message("Error al validar token").build();
        }
    }

    /**
     * Extrae el username de un token JWT sin validar firma
     */
    public String extractUsernameFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Verifica si un token está expirado
     */
    public boolean isTokenExpired(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public String getRoleFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("role", String.class);
        } catch (Exception e) {
            return null;
        }
    }

}