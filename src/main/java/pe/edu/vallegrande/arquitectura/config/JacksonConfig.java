package pe.edu.vallegrande.arquitectura.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
//import java.time.format.DateTimeParseException;
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Registrar el módulo de tiempo de Java
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        mapper.registerModule(javaTimeModule);
        
        // Crear un módulo personalizado para LocalDate
        SimpleModule customDateModule = new SimpleModule();
        customDateModule.addDeserializer(LocalDate.class, new CustomLocalDateDeserializer());
        mapper.registerModule(customDateModule);
        
        // Deshabilitar timestamps
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        return mapper;
    }
    
    // Deserializador personalizado que puede manejar tanto ISO dates como fechas simples
    public static class CustomLocalDateDeserializer extends JsonDeserializer<LocalDate> {
        @Override
        public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String date = p.getText();
            try {
                // Si viene con hora (formato ISO), extraer solo la fecha
                if (date.contains("T")) {
                    return LocalDate.parse(date.substring(0, 10), DateTimeFormatter.ISO_LOCAL_DATE);
                } else {
                    // Si viene solo la fecha
                    return LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
                }
            } catch (Exception e) {
                throw new IOException("Error parsing date: " + date, e);
            }
        }
    }
}
