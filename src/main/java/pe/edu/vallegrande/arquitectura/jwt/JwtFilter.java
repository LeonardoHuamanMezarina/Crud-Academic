package pe.edu.vallegrande.arquitectura.jwt;

import pe.edu.vallegrande.arquitectura.model.User;
import pe.edu.vallegrande.arquitectura.repository.UserRepository;
import pe.edu.vallegrande.arquitectura.service.EncryptionService;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import org.springframework.lang.NonNull;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    @Lazy  // 🔧 Lazy loading para romper la dependencia circular
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EncryptionService encryptionService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {

        // Ignorar solicitudes preflight CORS (OPTIONS) y endpoints públicos para
        // que CORS y otras configuraciones manejen la respuesta.
        String path = request.getRequestURI();
        String method = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method)
            || path.startsWith("/auth")
            || path.startsWith("/actuator")
            || path.startsWith("/h2-console")
            || path.startsWith("/swagger")
            || path.startsWith("/v3/api-docs")) {
            logger.debug("JwtFilter: skipping security for method={} path={}", method, path);
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        if (header == null) {
            logger.debug("JwtFilter: no Authorization header present for method={} path={}", method, path);
        } else if (!header.startsWith("Bearer ")) {
            logger.debug("JwtFilter: Authorization header does not start with 'Bearer ' : {}", header);
        }

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                String username = jwtUtil.extractUsername(token);
                logger.debug("JwtFilter: token presented, extracted username={}", username);

                if (username == null) {
                    logger.debug("JwtFilter: extracted username is null for token");
                }

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // 🔍 Buscar usuario por username ENCRIPTADO (no hasheado)
                    List<User> allUsers = userRepository.findAll();
                    User foundUser = null;

                    for (User user : allUsers) {
                        try {
                            // Desencriptar el username almacenado en BD para comparar con el del token
                            String decryptedUsername = encryptionService.decrypt(user.getUsername());
                            logger.debug("JwtFilter: comparing token username='{}' with decrypted db username='{}' (user id={})", username, decryptedUsername, user.getIdUsuario());
                            if (username.equals(decryptedUsername)) {
                                foundUser = user;
                                break;
                            }
                        } catch (Exception e) {
                            // Log at debug so we can see decryption issues in normal debug logs
                            logger.debug("JwtFilter: failed to decrypt username for user id={} (skipping): {}", user.getIdUsuario(), e.getMessage());
                            continue;
                        }
                    }

                    if (foundUser == null) {
                        logger.debug("JwtFilter: no matching user found in database for username={}", username);
                    } else {
                        boolean valid = jwtUtil.validateToken(token);
                        logger.debug("JwtFilter: token validation for user={} returned {}", username, valid);

                        if (foundUser != null && valid) {
                            // ✅ Usar getRoles() que es el campo correcto del modelo User
                            List<SimpleGrantedAuthority> authorities =
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + foundUser.getRoles()));

                            UsernamePasswordAuthenticationToken authToken =
                                    new UsernamePasswordAuthenticationToken(username, null, authorities);

                            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authToken);

                            logger.info("JwtFilter: authentication set for user={} authorities={}", username, authorities);
                        } else {
                            logger.debug("JwtFilter: token invalid or user null for username={}", username);
                        }
                    }
                } else {
                    logger.trace("JwtFilter: skipping auth creation because username is null or authentication already present");
                }
            } catch (ExpiredJwtException e) {
                logger.warn("JwtFilter: token expired: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            } catch (Exception e) {
                logger.error("JwtFilter: unexpected error while processing token: {}", e.getMessage(), e);
                // Do not interrupt the chain; return 401 to be explicit
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        chain.doFilter(request, response);
    }

}
