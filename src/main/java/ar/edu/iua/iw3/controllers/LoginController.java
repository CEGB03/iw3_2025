package ar.edu.iua.iw3.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.iua.iw3.controllers.dto.LoginRequestDTO;
import ar.edu.iua.iw3.controllers.dto.LoginResponseDTO;
import ar.edu.iua.iw3.controllers.dto.SignupRequestDTO;
import ar.edu.iua.iw3.controllers.dto.UserListResponseDTO;
import ar.edu.iua.iw3.controllers.dto.PaginatedResponse;
import ar.edu.iua.iw3.model.business.exceptions.BusinessException;
import ar.edu.iua.iw3.model.business.exceptions.UnauthorizedException;
import ar.edu.iua.iw3.model.business.interfaces.IAuthBusiness;
import ar.edu.iua.iw3.util.IStandartResponseBusiness;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(Constants.URL_LOGIN)
@Tag(name = "Authentication", description = "Endpoints de autenticación y autorización")
@Slf4j
public class LoginController {

    @Autowired
    private IAuthBusiness authBusiness;

    @Autowired
    private IStandartResponseBusiness response;

    @Operation(
        summary = "Autenticar usuario con JWT",
        description = """
            Realiza la autenticación del usuario y retorna un token JWT de acceso.
            
            **Usuarios de prueba disponibles:**
            - **admin** / admin123
            - **operator** / operator123
            - **viewer** / viewer123
            
            **Uso del token:**
            1. Copiar el token de la respuesta
            2. Incluir en el header `Authorization: Bearer <token>` en siguientes requests
            3. El token expira en 1 hora
            
            **Ejemplo con curl:**
            ```
            curl -X POST http://localhost:8080/api/v1/login \\
              -H "Content-Type: application/json" \\
              -d '{"username":"admin","password":"admin123"}'
            ```
            
            **Usar token en siguiente request:**
            ```
            curl -X GET http://localhost:8080/api/v1/orders \\
              -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5..."
            ```
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Autenticación exitosa - Token JWT generado",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = LoginResponseDTO.class,
                    example = """
                    {
                      "message": "Login exitoso",
                      "username": "admin",
                      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                      "expiresIn": 3600000
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Credenciales inválidas o token expirado",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(example = """
                {
                  "code": 401,
                  "message": "Usuario o contraseña incorrectos"
                }
                """)
            )
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Validación fallida (campos vacíos o inválidos)",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(example = """
                {
                  "code": 422,
                  "message": "El nombre de usuario no puede estar vacío"
                }
                """)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(example = """
                {
                  "code": 500,
                  "message": "Error en el proceso de autenticación"
                }
                """)
            )
        )
    })
    @PostMapping(
        value = "",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest) {
        try {
            // Validación básica de entrada
            if (loginRequest == null) {
                return new ResponseEntity<>(
                    response.build(HttpStatus.UNPROCESSABLE_ENTITY, null, "El body de la solicitud no puede estar vacío"),
                    HttpStatus.UNPROCESSABLE_ENTITY
                );
            }

            // Autenticar y generar JWT
            String token = authBusiness.authenticate(loginRequest.getUsername(), loginRequest.getPassword());

            // Construir respuesta exitosa
            LoginResponseDTO loginResponse = new LoginResponseDTO(
                "Login exitoso",
                loginRequest.getUsername(),
                token,
                3600000 // 1 hora en milisegundos
            );

            log.info("Usuario {} autenticado exitosamente con JWT", loginRequest.getUsername());
            return new ResponseEntity<>(loginResponse, HttpStatus.OK);

        } catch (UnauthorizedException e) {
            log.warn("Intento de login fallido: {}", e.getMessage());
            return new ResponseEntity<>(
                response.build(HttpStatus.UNAUTHORIZED, e, e.getMessage()),
                HttpStatus.UNAUTHORIZED
            );
        } catch (BusinessException e) {
            log.error("Error de negocio en login: {}", e.getMessage());
            return new ResponseEntity<>(
                response.build(HttpStatus.UNPROCESSABLE_ENTITY, e, e.getMessage()),
                HttpStatus.UNPROCESSABLE_ENTITY
            );
        } catch (Exception e) {
            log.error("Error inesperado en login: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, "Error interno en el servidor"),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> whoAmI() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return new ResponseEntity<>(response.build(HttpStatus.UNAUTHORIZED, null, "No autenticado"), HttpStatus.UNAUTHORIZED);
        }
        String username = String.valueOf(auth.getPrincipal());
        String role = auth.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse(null);

        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("username", username);
        body.put("role", role);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/test-admin")
    public ResponseEntity<?> testAdmin() {
        return new ResponseEntity<>("OK ADMIN", HttpStatus.OK);
    }

    @PreAuthorize("hasRole('OPERADOR')")
    @GetMapping("/test-operator")
    public ResponseEntity<?> testOperator() {
        return new ResponseEntity<>("OK OPERADOR", HttpStatus.OK);
    }

    @PreAuthorize("hasRole('VISOR')")
    @GetMapping("/test-viewer")
    public ResponseEntity<?> testViewer() {
        return new ResponseEntity<>("OK VISOR", HttpStatus.OK);
    }

    @Operation(
        summary = "Crear nuevo usuario (solo ADMIN)",
        description = """
            Crea un nuevo usuario en el sistema. Solo usuarios con rol ADMIN pueden acceder a este endpoint.
            
            **Roles disponibles:**
            - ADMIN: Acceso total, puede crear usuarios
            - OPERADOR: Puede registrar detalles de carga y ver órdenes
            - VISOR: Solo lectura de información
            
            **Validaciones:**
            - El nombre de usuario debe ser único
            - La contraseña se almacena de forma segura (BCrypt)
            - El rol debe ser uno de los valores válidos
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Usuario creado exitosamente",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(example = """
                {
                  "message": "Usuario creado exitosamente",
                  "username": "nuevo_usuario",
                  "role": "OPERADOR"
                }
                """)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validación fallida (usuario duplicado, rol inválido, etc)",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(example = """
                {
                  "code": 400,
                  "message": "El usuario ya existe"
                }
                """)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - solo ADMIN puede crear usuarios",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(example = """
                {
                  "code": 403,
                  "message": "Acceso denegado"
                }
                """)
            )
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Datos de entrada inválidos o incompletos",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(example = """
                {
                  "code": 422,
                  "message": "Campos requeridos: username, password, role"
                }
                """)
            )
        )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(
        value = "/signup",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> signup(@RequestBody SignupRequestDTO signupRequest) {
        try {
            // Validación básica de entrada
            if (signupRequest == null) {
                return new ResponseEntity<>(
                    response.build(HttpStatus.UNPROCESSABLE_ENTITY, null, "El body de la solicitud no puede estar vacío"),
                    HttpStatus.UNPROCESSABLE_ENTITY
                );
            }

            // Validar que los campos requeridos no estén vacíos
            if (signupRequest.getUsername() == null || signupRequest.getUsername().trim().isEmpty()) {
                return new ResponseEntity<>(
                    response.build(HttpStatus.UNPROCESSABLE_ENTITY, null, "El nombre de usuario es requerido"),
                    HttpStatus.UNPROCESSABLE_ENTITY
                );
            }

            if (signupRequest.getPassword() == null || signupRequest.getPassword().trim().isEmpty()) {
                return new ResponseEntity<>(
                    response.build(HttpStatus.UNPROCESSABLE_ENTITY, null, "La contraseña es requerida"),
                    HttpStatus.UNPROCESSABLE_ENTITY
                );
            }

            if (signupRequest.getRole() == null || signupRequest.getRole().trim().isEmpty()) {
                return new ResponseEntity<>(
                    response.build(HttpStatus.UNPROCESSABLE_ENTITY, null, "El rol es requerido"),
                    HttpStatus.UNPROCESSABLE_ENTITY
                );
            }

            // Crear el usuario
            authBusiness.signup(signupRequest.getUsername(), signupRequest.getPassword(), signupRequest.getRole());

            // Construir respuesta exitosa
            java.util.Map<String, Object> responseBody = new java.util.HashMap<>();
            responseBody.put("message", "Usuario creado exitosamente");
            responseBody.put("username", signupRequest.getUsername());
            responseBody.put("role", signupRequest.getRole().toUpperCase());

            log.info("Usuario {} creado exitosamente por admin", signupRequest.getUsername());
            return new ResponseEntity<>(responseBody, HttpStatus.CREATED);

        } catch (BusinessException e) {
            log.warn("Error al crear usuario: {}", e.getMessage());
            return new ResponseEntity<>(
                response.build(HttpStatus.BAD_REQUEST, e, e.getMessage()),
                HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            log.error("Error inesperado al crear usuario: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, "Error interno en el servidor"),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Operation(
        summary = "Listar todos los usuarios (solo ADMIN)",
        description = "Obtiene la lista completa de todos los usuarios del sistema. Solo administradores pueden acceder a este endpoint."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de usuarios obtenida exitosamente",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(example = """
                [
                  {
                    "id": "uuid-1",
                    "username": "admin",
                    "role": "ADMIN",
                    "enabled": true,
                    "createdAt": "2025-12-18T10:00:00",
                    "updatedAt": "2025-12-18T10:00:00"
                  },
                  {
                    "id": "uuid-2",
                    "username": "operador",
                    "role": "OPERADOR",
                    "enabled": true,
                    "createdAt": "2025-12-18T11:00:00",
                    "updatedAt": "2025-12-18T11:00:00"
                  }
                ]
                """)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - solo ADMIN puede listar usuarios",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(example = """
                {
                  "code": 403,
                  "message": "Acceso denegado"
                }
                """)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(example = """
                {
                  "code": 500,
                  "message": "Error al obtener la lista de usuarios"
                }
                """)
            )
        )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(
        value = "/users",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> getAllUsersPaginated(
            @Parameter(description = "Número de página (comenzando en 0)") 
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Cantidad de registros por página") 
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<UserListResponseDTO> usersPage = authBusiness.getAllUsersPaginated(pageable);
            
            PaginatedResponse<UserListResponseDTO> response = new PaginatedResponse<>(
                    usersPage.getContent(),
                    usersPage.getTotalPages(),
                    usersPage.getTotalElements(),
                    page,
                    size,
                    usersPage.isFirst(),
                    usersPage.isLast(),
                    usersPage.hasNext(),
                    usersPage.hasPrevious()
            );
            
            log.info("Lista paginada de usuarios obtenida - Total: {}", usersPage.getTotalElements());
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (BusinessException e) {
            log.error("Error al obtener usuarios: {}", e.getMessage());
            return new ResponseEntity<>(
                this.response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        } catch (Exception e) {
            log.error("Error inesperado al obtener usuarios: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                this.response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, "Error interno en el servidor"),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}