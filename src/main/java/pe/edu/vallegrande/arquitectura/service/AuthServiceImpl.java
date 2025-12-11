package pe.edu.vallegrande.arquitectura.service;

import pe.edu.vallegrande.arquitectura.jwt.JwtUtil;
import pe.edu.vallegrande.arquitectura.model.User;
import pe.edu.vallegrande.arquitectura.repository.UserRepository;
import pe.edu.vallegrande.arquitectura.dto.LoginResponse;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthServiceImpl {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EncryptionService encryptionService;
    
    @Value("${jwt.expiration}")
    private long expirationMs;

    /**
     * Listar todos los usuarios
     */
    public List<User> findAll() {
        log.info("Listando todos los usuarios");
        return userRepository.findAll();
    }

    /**
     * Registrar nuevo usuario - Username/Roles encriptados, Password hasheado
     */
    public User save(User user) {
        String originalUsername = user.getUsername(); // Guardar username original para logs
        
        log.info("Registrando nuevo usuario: {}", originalUsername);
        
        // Log completo de datos recibidos
        
        log.info("=== DATOS RECIBIDOS ===");
        log.info("Username: {}", user.getUsername());
        log.info("Password: {}", user.getContra() != null ? "[PRESENTE]" : "[AUSENTE]");
        log.info("Email: {}", user.getEmail());
        log.info("FirstName: {}", user.getFirstName());
        log.info("LastName: {}", user.getLastName());
        log.info("DocumentType: {}", user.getDocumentType());
        log.info("DocumentNumber: {}", user.getDocumentNumber());
        log.info("BirthDate: {}", user.getBirthDate());
        log.info("Phone: {}", user.getPhone());
        log.info("Address: {}", user.getAddress());
        log.info("=======================");
        
        // Validaciones básicas
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new RuntimeException("Username no puede estar vacío");
        }
        
        if (user.getContra() == null || user.getContra().trim().isEmpty()) {
            throw new RuntimeException("Password no puede estar vacío");
        }
        
        if (user.getBirthDate() == null) {
            throw new RuntimeException("BirthDate no puede estar vacío - requerido por Oracle");
        }

        // Asignar rol por defecto si no tiene
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles("CLIENTE");
        }
        
        // 🔐 SISTEMA DE SEGURIDAD MIXTO
        user.setUsername(encryptionService.encrypt(user.getUsername())); // Username ENCRIPTADO (reversible)
        user.setContra(passwordEncoder.encode(user.getContra()));        // Password HASHEADO (irreversible)
        
        // Establecer estado activo y fecha de registro
        user.setStatus('A');
        user.setRegistrationDate(LocalDateTime.now());
        
        
        log.info("=== DATOS ANTES DE GUARDAR ===");
        log.info("Username original: {}", originalUsername);
        log.info("Username hasheado: {}", user.getUsername());
        log.info("Email: {}", user.getEmail());
        log.info("FirstName: {}", user.getFirstName());
        log.info("LastName: {}", user.getLastName());
        log.info("DocumentType: {}", user.getDocumentType());
        log.info("DocumentNumber: {}", user.getDocumentNumber());
        log.info("BirthDate: {}", user.getBirthDate());
        log.info("Phone: {}", user.getPhone());
        log.info("Address: {}", user.getAddress());
        log.info("Status: {}", user.getStatus());
        log.info("Roles: {}", user.getRoles());
        log.info("RegistrationDate: {}", user.getRegistrationDate());
        log.info("Username/Roles ENCRIPTADOS - Password HASHEADO");
        log.info("==============================");
        
        User savedUser = userRepository.save(user);
        
        log.info("Usuario guardado exitosamente con ID: {}", savedUser.getIdUsuario());
        
        return savedUser;
    }

    /**
     * Login de usuario - valida credenciales con sistema mixto de seguridad
     */
    public LoginResponse login(String username, String password) {
        log.info("Intentando login para usuario: {}", username);
        
        // 🔍 Como el username está ENCRIPTADO (no hasheado), necesitamos buscar por todos los usuarios
        // y desencriptar cada username para comparar
        List<User> allUsers = userRepository.findAll();
        User foundUser = null;
        
        for (User user : allUsers) {
            try {
                // Desencriptar el username almacenado en BD para comparar
                String decryptedUsername = encryptionService.decrypt(user.getUsername());
                if (username.equals(decryptedUsername)) {
                    foundUser = user;
                    break;
                }
            } catch (Exception e) {
                log.debug("Error desencriptando username para usuario ID: {}", user.getIdUsuario());
                // Continuar con el siguiente usuario si hay error de desencriptación
            }
        }
        
        if (foundUser == null) {
            log.warn("Usuario no encontrado: {}", username);
            throw new RuntimeException("Usuario no encontrado: " + username);
        }
        
        // Verificar que el usuario esté activo
        if (foundUser.getStatus() != 'A') {
            throw new RuntimeException("Usuario inactivo");
        }
        
        // Validar contraseña HASHEADA (usando matches)
        if (!passwordEncoder.matches(password, foundUser.getContra())) {
            log.warn("Contraseña incorrecta para usuario: {}", username);
            throw new RuntimeException("Credenciales incorrectas");
        }
        
        // Roles NO están encriptados (por restricción Oracle)
        String userRoles = foundUser.getRoles();
        
        // Generar JWT token con el username ORIGINAL (el que viene del request)
        String token = jwtUtil.generateToken(username);
        
        log.info("Login exitoso para usuario: {} (Nombre: {} {})", username, foundUser.getFirstName(), foundUser.getLastName());
        
        // Crear respuesta con token, username original y roles originales
        return new LoginResponse(token, username, userRoles, expirationMs);
    }
    
    /**
     * Registrar nuevo ADMIN - Username/Roles encriptados, Password hasheado
     */
    public User saveAdmin(User user) {
        String originalUsername = user.getUsername(); // Guardar username original para logs
        
        log.info("Registrando nuevo ADMIN: {}", originalUsername);

        user.setRoles("ADMIN"); // ⚡ ROL ADMIN
        
        // Validaciones básicas
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new RuntimeException("Username no puede estar vacío");
        }
        
        if (user.getContra() == null || user.getContra().trim().isEmpty()) {
            throw new RuntimeException("Password no puede estar vacío");
        }
        
        // 🔐 SISTEMA DE SEGURIDAD MIXTO PARA ADMIN
        user.setUsername(encryptionService.encrypt(user.getUsername())); // Username ENCRIPTADO (reversible)
        user.setContra(passwordEncoder.encode(user.getContra()));        // Password HASHEADO (irreversible)
        
        // Establecer como ADMIN activo
        user.setStatus('A');
        user.setRegistrationDate(LocalDateTime.now());
        
        log.info("ADMIN creado - Username original: {}, Username encriptado: {}", 
                originalUsername, user.getUsername());
        
        return userRepository.save(user);
    }

    /**
     * Buscar usuario por username
     */
    public User findByUsername(String username) {
        log.info("Buscando usuario por username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
    }

}