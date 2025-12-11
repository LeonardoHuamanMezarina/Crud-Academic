package pe.edu.vallegrande.arquitectura.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID_usuario
    @Column(name = "id_users")
    private Long idUsuario;

    @Column(name = "username", length = 255, nullable = false) // nombre de usuario
    private String username;

    @Column(name = "password", length = 255, nullable = false) // Contraseña de usuario
    private String contra;

    @Column(name = "email", nullable = false) // Correo
    private String email;

    @Column(name = "first_name", nullable = false) // Nombre
    private String firstName;

    @Column(name = "last_name", nullable = false) // Apellido
    private String lastName;

    @Column(name = "document_type") // Tipo de documento
    private String documentType;

    @Column(name = "document_number", nullable = false) // Número de documento (DNI - base para username/password)
    private String documentNumber;

    @Column(name = "birth_date", nullable = false) // Fecha de nacimiento
    private LocalDate birthDate;

    @Column(name = "phone") // Télefono
    private String phone;

    @Column(name = "status", nullable = false) // estado ('A' = Activo, 'I' = Inactivo)
    private char status = 'A'; // Valor por defecto: Activo

    @Column(name = "roles", nullable = false) // Roles ('ADMIN' o 'CLIENTE')
    private String roles = "CLIENTE"; // Valor por defecto: CLIENTE

    @Column(name = "address") // Dirección
    private String address;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate; // Fecha de registro

}   
