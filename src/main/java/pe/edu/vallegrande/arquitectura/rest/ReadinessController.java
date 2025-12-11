package pe.edu.vallegrande.arquitectura.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint ligero para readiness/basic health checks que no depende de la DB.
 * Útil para plataformas (Railway) que necesitan un path rápido para comprobar
 * si la aplicación está arrancada.
 */
@RestController
public class ReadinessController {

    @GetMapping("/healthcheck")
    public ResponseEntity<String> healthcheck() {
        return ResponseEntity.ok("OK");
    }
}
