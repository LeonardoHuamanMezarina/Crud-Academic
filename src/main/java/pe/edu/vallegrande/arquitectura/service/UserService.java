package pe.edu.vallegrande.arquitectura.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import pe.edu.vallegrande.arquitectura.model.User;
import pe.edu.vallegrande.arquitectura.repository.UserRepository;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

@Service
@Slf4j
@AllArgsConstructor

public class UserService {
    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final pe.edu.vallegrande.arquitectura.service.EncryptionService encryptionService;
    private DataSource dataSource;

    /* Listar mediante || Total || ID || EStado */

    public List<User> getAll() {
        // Devolver usuarios ordenados por fecha de registro (más nuevo primero)
        List<User> users = userRepository.findByRolesOrderByRegistrationDateDesc("CLIENTE");
        log.info(users.toString());
        log.info("Listado de usuarios completado :>");
        return users;
    }

    /**
     * Obtener todos los usuarios (todos los roles) ordenados por fecha de registro descendente
     */
    public List<User> getAllOrderedByRegistrationDateDesc() {
        return userRepository.findAll().stream()
                .sorted((a,b) -> b.getRegistrationDate().compareTo(a.getRegistrationDate()))
                .toList();
    }

    /*Listar usuarios mediante x ID */ 
    public Optional<User> findById(Long id) { // ← Cambia a Optional<User>
        log.info("Buscando usuario con ID:{}", id);
        return userRepository.findById(id);
    }

    public List<User> findByStatus(String status) {
        log.info("Listando usuarios por estado:{}", status);
        Character statusChar = status != null && !status.isEmpty() ? status.charAt(0) : null;
        return userRepository.findByStatus(statusChar); // ← Pasa Character al repositorio
    }

    /* Listado de todos los clientes */
    public List<User> findByRoles(String roles) {
        log.info("Listando usuarios por rol: {}", roles);
        return userRepository.findByRoles(roles);
    }

    /* Método específico para listar solo clientes */
    public List<User> getAllClients() {
        log.info("Listando todos los usuarios con rol CLIENTE");
        return userRepository.findByRoles("CLIENTE");
    }

    /* Creando usuario */

    public User create(User user) {
        user.setStatus('A'); // 'A' para activo
        user.setRoles("CLIENTE"); // Por defecto se crea como Activo
        user.setRegistrationDate(LocalDateTime.now());
        log.info("Creando usuario: {}", user);
        return userRepository.save(user);
    }

    /* Crear cliente con username=email y password=documento */
    public User createClient(User user) {
        // Validaciones básicas
        if (user.getDocumentNumber() == null || user.getDocumentNumber().trim().isEmpty()) {
            throw new RuntimeException("DocumentNumber es requerido");
        }
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            throw new RuntimeException("FirstName es requerido");
        }
        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            throw new RuntimeException("LastName es requerido");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email es requerido");
        }

        // Usar DNI como credencial: username = DNI, password = DNI
        String dni = user.getDocumentNumber().trim();

        // Encriptar username (reversible) y hashear la contraseña (irreversible)
        String encryptedUsername = encryptionService.encrypt(dni);
        String hashedPassword = passwordEncoder.encode(dni);

        user.setUsername(encryptedUsername);
        user.setContra(hashedPassword);

        user.setStatus('A');
        user.setRoles("CLIENTE"); // Por defecto se crea como Activo
        user.setRegistrationDate(LocalDateTime.now());
        log.info("Creando cliente: {}", user);
        User saved = userRepository.save(user);
        return saved;
    }

    /* Editar usuario */

    public User update(User user) {
        return userRepository.findById(user.getIdUsuario()).map(existing -> {

            // Actualizar solo los campos que el usuario puede modificar
            existing.setUsername(user.getUsername());
            existing.setContra(user.getContra());
            existing.setEmail(user.getEmail());
            existing.setFirstName(user.getFirstName());
            existing.setLastName(user.getLastName());
            existing.setDocumentType(user.getDocumentType());
            existing.setDocumentNumber(user.getDocumentNumber());
            existing.setBirthDate(user.getBirthDate()); // Agregar la fecha de nacimiento
            existing.setPhone(user.getPhone());
            existing.setAddress(user.getAddress());

            // **No actualizar `status` ni `registrationDate`**
            // Se conservan los valores originales en caso el usuario no los envíe
            existing.setRegistrationDate(existing.getRegistrationDate());

            return userRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    /* Editar cliente - solo campos específicos */
    public User updateClient(User user) {
        return userRepository.findById(user.getIdUsuario()).map(existing -> {

            // Actualizar solo los campos permitidos para clientes
            existing.setFirstName(user.getFirstName());
            existing.setLastName(user.getLastName());
            existing.setEmail(user.getEmail()); // Agregar email
            existing.setDocumentType(user.getDocumentType());
            existing.setDocumentNumber(user.getDocumentNumber());
            existing.setBirthDate(user.getBirthDate());
            existing.setPhone(user.getPhone());
            existing.setAddress(user.getAddress());

            // NO actualizar: username, contra, status, roles, registrationDate
            // Estos campos se mantienen como estaban originalmente

            log.info("Actualizando cliente: {}", existing);
            return userRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
    }

    /* Eliminar usuario */

    public void delete(Long id) {
        log.info("Eliminando lógicamente cliente con ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        user.setStatus('I'); // Cambiar estado a Inactivo
        userRepository.save(user);
    }

    /* Restaurar usuario por ID */

    public void restore(Long id) {
        log.info("Restaurando usuario con ID: {}", id);

        Optional<User> optional = userRepository.findById(id);
        if (optional.isPresent()) {
            User user = optional.get();
            user.setStatus('A'); // Cambiar el estado a 'A' (activo)
            userRepository.save(user); // Guardar el cambio
            log.info("Usuario restaurado con ID: {}", id);
        } else {
            log.warn("No se encontró el usuario con ID: {}", id);
        }
    }

    public byte[] generateJasperPdfReport() throws Exception {
        InputStream jasperStream = new ClassPathResource("reports/Clientes.jasper").getInputStream();
        HashMap<String, Object> params = new HashMap<>();
        try (var conn = dataSource.getConnection()) {
            log.info("Conectado a: {}", conn.getMetaData().getURL());
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperStream, params, conn);
            log.info("Páginas generadas: {}", jasperPrint.getPages().size());
            return JasperExportManager.exportReportToPdf(jasperPrint);
        }
    }

    // MÉTODO PARA REPORTE TRANSACCIONAL (TICKET DE VENTA)
    public byte[] generateSaleTicketReport(Long saleId) throws Exception {
        InputStream jasperStream = new ClassPathResource("reports/transaccional.jasper").getInputStream();
        HashMap<String, Object> params = new HashMap<>();
        params.put("saleid", saleId.intValue()); // Convertir Long a Integer

        try (var conn = dataSource.getConnection()) {
            log.info("Generando ticket para venta ID: {}", saleId);
            log.info("Conectado a: {}", conn.getMetaData().getURL());
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperStream, params, conn);
            log.info("Páginas generadas: {}", jasperPrint.getPages().size());
            return JasperExportManager.exportReportToPdf(jasperPrint);
        }
    }

    public void deleteLogic(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setStatus('I'); // Cambia el estado a 'I' (Inactivo)
        userRepository.save(user);
    }
}
