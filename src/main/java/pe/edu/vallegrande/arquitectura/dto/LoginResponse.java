package pe.edu.vallegrande.arquitectura.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    /**
     * Token JWT generado para el usuario autenticado
     */
    private String token;
    
    /**
     * Tipo de token (siempre será "Bearer")
     */
    private String tokenType = "Bearer";
    
    /**
     * Username del usuario autenticado
     */
    private String username;
    
    /**
     * Rol del usuario autenticado
     */
    private String role;
    
    /**
     * Tiempo de expiración del token en milisegundos
     */
    private Long expiresIn;

    /**
     * Constructor personalizado para crear respuesta con token
     */
    public LoginResponse(String token, String username, String role, Long expiresIn) {
        this.token = token;
        this.tokenType = "Bearer";
        this.username = username;
        this.role = role;
        this.expiresIn = expiresIn;
    }
}