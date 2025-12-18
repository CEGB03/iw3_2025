package ar.edu.iua.iw3.model.business.implementations;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.iua.iw3.model.business.exceptions.BusinessException;
import ar.edu.iua.iw3.model.business.exceptions.UnauthorizedException;
import ar.edu.iua.iw3.model.business.interfaces.IAuthBusiness;
import org.springframework.security.crypto.password.PasswordEncoder;
import ar.edu.iua.iw3.model.auth.UserAccount;
import ar.edu.iua.iw3.model.persistence.UserAccountRepository;
import ar.edu.iua.iw3.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthBusiness implements IAuthBusiness {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private UserAccountRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // @Override
    // public String authenticate(String username, String password) throws UnauthorizedException, BusinessException {
    //     try {
    //         // Validar entrada
    //         if (username == null || username.isEmpty()) {
    //             throw UnauthorizedException.builder()
    //                     .message("El nombre de usuario no puede estar vacío")
    //                     .build();
    //         }
            
    //         if (password == null || password.isEmpty()) {
    //             throw UnauthorizedException.builder()
    //                     .message("La contraseña no puede estar vacía")
    //                     .build();
    //         }
            
    //         // Buscar usuario y validar contraseña
    //         if (!VALID_USERS.containsKey(username)) {
    //             log.warn("Intento de login con usuario inexistente: {}", username);
    //             throw UnauthorizedException.builder()
    //                     .message("Usuario o contraseña incorrectos")
    //                     .build();
    //         }
            
    //         String storedPassword = VALID_USERS.get(username);
    //         if (!storedPassword.equals(password)) {
    //             log.warn("Intento de login fallido para usuario: {}", username);
    //             throw UnauthorizedException.builder()
    //                     .message("Usuario o contraseña incorrectos")
    //                     .build();
    //         }
            
    //         // ✅ Generar token JWT
    //         String token = jwtTokenProvider.generateToken(username);
    //         log.info("Usuario {} autenticado exitosamente con JWT", username);
            
    //         return token;
            
    //     } catch (UnauthorizedException e) {
    //         throw e;
    //     } catch (Exception e) {
    //         log.error("Error durante autenticación: {}", e.getMessage(), e);
    //         throw BusinessException.builder()
    //                 .ex(e)
    //                 .message("Error en el proceso de autenticación")
    //                 .build();
    //     }
    // }

    @Override
public String authenticate(String username, String password) throws UnauthorizedException, BusinessException {
    try {
        // Validar entrada
        if (username == null || username.isEmpty()) {
            throw UnauthorizedException.builder()
                    .message("El nombre de usuario no puede estar vacío")
                    .build();
        }

        if (password == null || password.isEmpty()) {
            throw UnauthorizedException.builder()
                    .message("La contraseña no puede estar vacía")
                    .build();
        }

        // Buscar usuario
            // Buscar usuario en BD
            Optional<UserAccount> opt = userRepo.findByUsername(username);
            if (opt.isEmpty()) {
                log.warn("Intento de login con usuario inexistente: {}", username);
                throw UnauthorizedException.builder()
                        .message("Usuario o contraseña incorrectos")
                        .build();
            }

            UserAccount user = opt.get();

            if (!user.isEnabled()) {
                throw UnauthorizedException.builder()
                        .message("Usuario deshabilitado")
                        .build();
            }

            // Validar contraseña con BCrypt
            if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                log.warn("Intento de login fallido para usuario: {}", username);
                throw UnauthorizedException.builder()
                        .message("Usuario o contraseña incorrectos")
                        .build();
            }

            // Generar token JWT con rol
            String token = jwtTokenProvider.generateToken(
                    user.getUsername(),
                    user.getRole().name()
            );

        log.info("Usuario {} autenticado exitosamente con JWT", username);

        return token;

    } catch (UnauthorizedException e) {
        throw e;
    } catch (Exception e) {
        log.error("Error durante autenticación: {}", e.getMessage(), e);
        throw BusinessException.builder()
                .ex(e)
                .message("Error en el proceso de autenticación")
                .build();
    }
}

    @Override
    public boolean validateToken(String token) throws BusinessException {
        try {
            if (token == null || token.isEmpty()) {
                return false;
            }
            
            jwtTokenProvider.validateAndGetUsername(token);
            return true;
            
        } catch (UnauthorizedException e) {
            log.warn("Token inválido: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Error validando token: {}", e.getMessage(), e);
            throw BusinessException.builder()
                    .ex(e)
                    .message("Error al validar token")
                    .build();
        }
    }
}