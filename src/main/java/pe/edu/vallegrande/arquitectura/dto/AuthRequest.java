package pe.edu.vallegrande.arquitectura.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class AuthRequest {

    private String username;

    // Accept both 'password' and legacy field name 'contra' from clients
    @JsonAlias({"contra", "password"})
    private String password;

}