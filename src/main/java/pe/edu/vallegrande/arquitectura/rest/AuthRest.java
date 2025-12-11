package pe.edu.vallegrande.arquitectura.rest;

import pe.edu.vallegrande.arquitectura.model.User;
import pe.edu.vallegrande.arquitectura.dto.AuthRequest;
import pe.edu.vallegrande.arquitectura.dto.LoginResponse;
import pe.edu.vallegrande.arquitectura.service.AuthServiceImpl;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthRest {

    @Autowired
    private AuthServiceImpl authServiceImpl;

    /**
     * Listar todos los usuarios (solo para desarrollo/admin)
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> findAll(){
        try {
            List<User> users = authServiceImpl.findAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error al listar usuarios: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Registrar nuevo usuario
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            // Validaciones básicas
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Username es requerido"));
            }
            
            if (user.getContra() == null || user.getContra().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Contraseña es requerida"));
            }
            
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Email es requerido"));
            }
            
            log.info("Registrando usuario: {} con email: {}", user.getUsername(), user.getEmail());
            
            User newUser = authServiceImpl.save(user);
            log.info("Usuario registrado exitosamente: {}", newUser.getUsername());
            return ResponseEntity.ok(Map.of(
                "message", "Usuario registrado exitosamente",
                "username", newUser.getUsername(),
                "email", newUser.getEmail()
            ));
        } catch (Exception e) {
            log.error("Error al registrar usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Login - Autenticar usuario y generar JWT
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            // Validar que los campos requeridos estén presentes
            if (request.getUsername() == null || request.getPassword() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Username y password son requeridos"));
            }

            LoginResponse loginResponse = authServiceImpl.login(request.getUsername(), request.getPassword());
            log.info("Login exitoso para usuario: {}", request.getUsername());
            
            return ResponseEntity.ok(loginResponse);
            
        } catch (RuntimeException e) {
            log.error("Error en login para usuario {}: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error interno en login: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }
    
    /**
     * Registro de ADMIN - Mismos campos que cliente pero rol ADMIN
     */
    @PostMapping("/admin/register")
    public ResponseEntity<?> registerAdmin(@RequestBody User user) {
        try {
            log.info("=== REGISTRANDO ADMIN ===");
            
            // Validaciones básicas (mismas que cliente)
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Username es requerido"));
            }
            
            if (user.getContra() == null || user.getContra().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Contraseña es requerida"));
            }
            
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Email es requerido"));
            }
            
            if (user.getBirthDate() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Fecha de nacimiento es requerida"));
            }
            
            log.info("Registrando ADMIN: {} con email: {}", user.getUsername(), user.getEmail());
            
            User newAdmin = authServiceImpl.saveAdmin(user);
            log.info("ADMIN registrado exitosamente: {}", newAdmin.getUsername());
            
            return ResponseEntity.ok(Map.of(
                "message", "Administrador registrado exitosamente",
                "username", newAdmin.getUsername(),
                "email", newAdmin.getEmail(),
                "roles", newAdmin.getRoles()
            ));
            
        } catch (Exception e) {
            log.error("Error al registrar admin: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtener información del usuario autenticado (requiere JWT)
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {
            // Extraer username del token (sin el "Bearer ")
            String jwt = token.substring(7);
            // Aquí puedes usar JwtUtil para extraer el username y obtener los datos del usuario
            return ResponseEntity.ok(Map.of("message", "Endpoint para obtener datos del usuario actual"));
        } catch (Exception e) {
            log.error("Error al obtener usuario actual: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token inválido"));
        }
    }
}