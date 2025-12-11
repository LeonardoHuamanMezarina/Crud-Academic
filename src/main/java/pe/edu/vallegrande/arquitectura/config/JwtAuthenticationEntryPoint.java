package pe.edu.vallegrande.arquitectura.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Maneja las excepciones de autenticación JWT
 * Se ejecuta cuando un usuario no autenticado intenta acceder a un endpoint protegido
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // Log para depuración: método, URI, presencia del header Authorization y mensaje de la excepción
        String authHeader = request.getHeader("Authorization");
        logger.warn("JwtAuthenticationEntryPoint: auth failure method={} uri={} AuthorizationPresent={} exception={}",
                request.getMethod(), request.getRequestURI(), authHeader != null, authException == null ? "null" : authException.getMessage());

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "No autorizado");
        errorResponse.put("message", "Token JWT requerido para acceder a este recurso");
        errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        errorResponse.put("path", request.getRequestURI());
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), errorResponse);
    }
}