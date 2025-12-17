package ar.edu.iua.iw3.config;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return new Jackson2ObjectMapperBuilderCustomizer() {
            @Override
            public void customize(Jackson2ObjectMapperBuilder builder) {
                builder.modules(new JavaTimeModule());
                builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                builder.simpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                // ⚠️ COMENTADO: Usar snake_case fuerza a que todos los JSON usen guiones bajos
                // Si descomentas esto, debes enviar "product_name", "mass_accumulated", etc.
                // builder.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            }
        };
    }
}
