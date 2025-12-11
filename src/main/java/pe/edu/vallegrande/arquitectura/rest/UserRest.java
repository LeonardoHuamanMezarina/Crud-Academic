package pe.edu.vallegrande.arquitectura.rest;

import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.arquitectura.model.User;
import pe.edu.vallegrande.arquitectura.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;

/*
*   GET  ==> Listar
*   POST ==> Crear
*   PUT  ==> Editar
*   DELETE ==> Eliminar
*/

@RestController // Marca esta clase como un controlador REST que devuelve datos JSON (GET, POST,
                // PUT, DELETE)
@AllArgsConstructor // Genera un constructor con todos los atributos de la clase
@CrossOrigin("*") // Me permite utilizar las rutas de cualquier dominio
@RequestMapping("api/v1/user")

public class UserRest {

    private final UserService userService;

    /* Listar Clientes */
    @GetMapping("/listar")
    public List<User> getAll() {
        // devuelve clientes ordenados por fecha de registro (más nuevo primero)
        return userService.getAll();
    }

    /* Listar todos los usuarios (todos los roles) ordenados por fecha de registro desc */
    @GetMapping("/listar/ordenados")
    public List<User> getAllOrdered() {
        return userService.getAllOrderedByRegistrationDateDesc();
    }

    /* Listado por ID */
    @GetMapping("/listar/id/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        Optional<User> user = userService.findById(id); // ← Ahora devuelve Optional<User>
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Usuario no encontrado con ID: " + id);
            return ResponseEntity.notFound().build();
        }
    }

    /* Listado por Estado -> Activo = "A" Desconcetado = "D" */
    @GetMapping("/listar/estado/{status}")
    public List<User> findByStatus(@PathVariable("status") String status) { // ← String
        return userService.findByStatus(status);
    }

    /* Creando usuario POST */

    @PostMapping("/crear")
    public User create(@RequestBody User user) {
        return userService.create(user);
    }

    /* Crear cliente POST - username=email, password=documento */
    @PostMapping("/crear/cliente")
    public ResponseEntity<?> createClient(@RequestBody User user) {
        User created = userService.createClient(user);
        // No devolver la contraseña ni campos sensibles
        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "Cliente creado exitosamente");
        resp.put("id", created.getIdUsuario());
        // devolver username visible como DNI (no el username encriptado)
        resp.put("username", user.getDocumentNumber());
        resp.put("email", created.getEmail());
        return ResponseEntity.ok(resp);
    }

    /* Editar usuario PUT */

    @PutMapping("/editar")
    public User update(@RequestBody User user) {
        return userService.update(user);
    }

    /* Editar cliente PUT - solo campos específicos */
    @PutMapping("/editar/cliente")
    public User updateClient(@RequestBody User user) {
        return userService.updateClient(user);
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> generateJasperPdfReport() {
        try {
            byte[] pdf = userService.generateJasperPdfReport();
            return ResponseEntity.ok()
                    // Renombrar el archivo PDF al descargar
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_clientes.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /* NUEVO ENDPOINT PARA REPORTE TRANSACCIONAL */
    @GetMapping("/ticket/{saleId}")
    public ResponseEntity<byte[]> generateSaleTicketReport(@PathVariable Long saleId) {
        try {
            byte[] pdf = userService.generateSaleTicketReport(saleId);
            return ResponseEntity.ok()
                    // Renombrar el archivo PDF al descargar
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ticket_venta_" + saleId + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /* Listar solo usuarios con rol CLIENTE */
    @GetMapping("/listar/clientes")
    public List<User> getAllClients() {
        return userService.getAllClients();
    }

    /* Eliminar usuario lógico PATCH */
    @PatchMapping("/eliminar/{id}")
    public ResponseEntity<?> deleteLogic(@PathVariable Long id) {
        userService.deleteLogic(id);
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Usuario eliminado lógicamente con ID: " + id);
        return ResponseEntity.ok(response);
    }

    /* Restaurar usuario lógico PATCH */
    @PatchMapping("/restaurar/{id}")
    public ResponseEntity<?> restore(@PathVariable Long id) {
        userService.restore(id);
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Usuario restaurado con ID: " + id);
        return ResponseEntity.ok(response);
    }

}
