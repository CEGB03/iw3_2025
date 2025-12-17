package ar.edu.iua.iw3.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.iua.iw3.controllers.dto.LoginRequestDTO;
import ar.edu.iua.iw3.controllers.dto.LoginResponseDTO;
import ar.edu.iua.iw3.model.business.exceptions.BusinessException;
import ar.edu.iua.iw3.model.business.exceptions.UnauthorizedException;
import ar.edu.iua.iw3.model.business.interfaces.IAuthBusiness;
import ar.edu.iua.iw3.util.IStandartResponseBusiness;

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
}