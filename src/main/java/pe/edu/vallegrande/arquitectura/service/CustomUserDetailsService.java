package pe.edu.vallegrande.arquitectura.service;

import pe.edu.vallegrande.arquitectura.model.User;
import pe.edu.vallegrande.arquitectura.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * UserDetailsService personalizado que busca el username desencriptando los
 * nombres de usuario almacenados en la BD. Esto permite que Spring Security
 * construya un AuthenticationManager con un DaoAuthenticationProvider y así
 * evitar el mensaje "No authenticationProviders... Returning null".
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EncryptionService encryptionService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<User> all = userRepository.findAll();
        for (User u : all) {
            try {
                String dec = encryptionService.decrypt(u.getUsername());
                if (username.equals(dec)) {
                    // Construir UserDetails usando la contraseña hasheada almacenada
                    return org.springframework.security.core.userdetails.User
                            .withUsername(dec)
                            .password(u.getContra())
                            .roles(u.getRoles())
                            .build();
                }
            } catch (Exception e) {
                // ignorar problemas de desencriptación para este registro y continuar
                continue;
            }
        }
        throw new UsernameNotFoundException("Usuario no encontrado: " + username);
    }
}
