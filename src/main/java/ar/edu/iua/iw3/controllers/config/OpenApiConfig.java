package ar.edu.iua.iw3.controllers.config;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("IW3 Backend - API de Gestión de Órdenes")
                        .version("1.0.0")
                        .description("""
                                API REST para la gestión completa del ciclo de vida de órdenes de carga de productos.
                                
                                ## Flujo de estados:
                                1. **Estado 1**: Orden creada (pendiente de pesaje inicial)
                                2. **Estado 2**: Pesaje inicial registrado (carga en progreso)
                                3. **Estado 3**: Carga cerrada (pendiente de pesaje final)
                                4. **Estado 4**: Orden finalizada (conciliación disponible)
                                
                                ## Características:
                                - Gestión de Clientes, Conductores, Camiones y Productos
                                - Control de flujo de carga con contraseña de activación
                                - Registro de detalles en tiempo real
                                - Conciliación automática entre balanza y caudalímetro
                                """)
                        .contact(new Contact()
                                .name("IUA - Ingeniería Web 3")
                                .email("cgomez453@alumnos.iua.edu.ar"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de Desarrollo"),
                        new Server()
                                .url("https://api.example.com")
                                .description("Servidor de Producción (ejemplo)")
                ))
                .tags(List.of(
                        new Tag().name("Orders").description("Gestión de órdenes de carga"),
                        new Tag().name("Customers").description("Gestión de clientes"),
                        new Tag().name("Drivers").description("Gestión de conductores"),
                        new Tag().name("Trucks").description("Gestión de camiones"),
                        new Tag().name("Products").description("Gestión de productos")
                ));
    }
}
