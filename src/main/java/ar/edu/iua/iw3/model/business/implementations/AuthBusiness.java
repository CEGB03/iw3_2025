package ar.edu.iua.iw3.model.business.implementations;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import ar.edu.iua.iw3.controllers.dto.UserListResponseDTO;
import ar.edu.iua.iw3.model.business.exceptions.BusinessException;
import ar.edu.iua.iw3.model.business.exceptions.UnauthorizedException;
import ar.edu.iua.iw3.model.business.interfaces.IAuthBusiness;
import org.springframework.security.crypto.password.PasswordEncoder;
import ar.edu.iua.iw3.model.auth.UserAccount;
import ar.edu.iua.iw3.model.persistence.UserAccountRepository;
import ar.edu.iua.iw3.security.JwtTokenProvider;
import ar.edu.iua.iw3.security.Role;
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

    @Override
    public void signup(String username, String password, String roleStr) throws BusinessException {
        try {
            // Validar entrada
            if (username == null || username.trim().isEmpty()) {
                throw BusinessException.builder()
                        .message("El nombre de usuario no puede estar vacío")
                        .build();
            }
            if (password == null || password.trim().isEmpty()) {
                throw BusinessException.builder()
                        .message("La contraseña no puede estar vacía")
                        .build();
            }
            if (roleStr == null || roleStr.trim().isEmpty()) {
                throw BusinessException.builder()
                        .message("El rol no puede estar vacío")
                        .build();
            }

            // Verificar si usuario ya existe
            if (userRepo.existsByUsername(username)) {
                throw BusinessException.builder()
                        .message("El usuario ya existe")
                        .build();
            }

            // Convertir string a Role enum
            Role role;
            try {
                role = Role.valueOf(roleStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw BusinessException.builder()
                        .message("Rol inválido. Roles disponibles: ADMIN, OPERADOR, VISOR")
                        .build();
            }

            // Crear nuevo usuario
            UserAccount user = new UserAccount();
            user.setUsername(username);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setRole(role);
            user.setEnabled(true);

            userRepo.save(user);
            log.info("Usuario {} creado exitosamente con rol {}", username, role);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creando usuario: {}", e.getMessage(), e);
            throw BusinessException.builder()
                    .ex(e)
                    .message("Error al crear el usuario")
                    .build();
        }
    }

    @Override
    public List<UserListResponseDTO> getAllUsers() throws BusinessException {
        try {
            return userRepo.findAll().stream()
                    .map(user -> new UserListResponseDTO(
                            user.getId(),
                            user.getUsername(),
                            user.getRole().toString(),
                            user.isEnabled(),
                            user.getCreatedAt(),
                            user.getUpdatedAt()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error al obtener lista de usuarios: {}", e.getMessage(), e);
            throw BusinessException.builder()
                    .ex(e)
                    .message("Error al obtener la lista de usuarios")
                    .build();
        }
    }

    @Override
    public Page<UserListResponseDTO> getAllUsersPaginated(Pageable pageable) throws BusinessException {
        try {
            Page<UserAccount> usersPage = userRepo.findAll(pageable);
            List<UserListResponseDTO> dtos = usersPage.getContent().stream()
                    .map(user -> new UserListResponseDTO(
                            user.getId(),
                            user.getUsername(),
                            user.getRole().toString(),
                            user.isEnabled(),
                            user.getCreatedAt(),
                            user.getUpdatedAt()
                    ))
                    .collect(Collectors.toList());
            return new PageImpl<>(dtos, pageable, usersPage.getTotalElements());
        } catch (Exception e) {
            log.error("Error al obtener lista paginada de usuarios: {}", e.getMessage(), e);
            throw BusinessException.builder()
                    .ex(e)
                    .message("Error al obtener la lista de usuarios")
                    .build();
        }
    }
}