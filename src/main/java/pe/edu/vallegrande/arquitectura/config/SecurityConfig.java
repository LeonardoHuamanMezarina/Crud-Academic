package pe.edu.vallegrande.arquitectura.config;

import pe.edu.vallegrande.arquitectura.jwt.JwtFilter;

import org.apache.commons.collections4.Get;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    /**
     * Configuración de la cadena de filtros de seguridad
     * Define qué endpoints son públicos y cuáles requieren autenticación
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Habilitar CORS usando el CorsConfigurationSource definido abajo y desactivar
        // CSRF
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable()) // Desactivar CSRF para APIs REST
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Sin
                                                                                                              // sesiones
                .authorizeHttpRequests(authz -> authz
                        // Permitir todas las solicitudes preflight OPTIONS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Endpoints públicos (no requieren autenticación)
                        .requestMatchers("/auth/register").permitAll() // Registro de clientes
                        .requestMatchers("/auth/login").permitAll() // Login general
                        .requestMatchers("/auth/users").permitAll() // Listar usuarios
                        .requestMatchers("/auth/admin/register").permitAll() // Registro de admins
                        .requestMatchers("/actuator/**").permitAll() // Actuator endpoints
                        .requestMatchers("/h2-console/**").permitAll() // H2 console si usas H2
                        // Permitir acceso público a la ruta de errores para que las redirecciones internas
                        // a /error no soliciten autenticación y no causen bucles 401.
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/error/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/user/pdf").permitAll() // permitir descarga pública de
                                                                                         // PDF

                        // Endpoints específicos para CLIENTE (DEBE IR ANTES del /**)
                        // CLIENTE puede VER productos
                        .requestMatchers(HttpMethod.GET, "/api/v1/product/listar").hasAnyRole("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/product/listar/**").hasAnyRole("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/product/disponibles").hasAnyRole("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/product/pdf").hasAnyRole("CLIENTE", "ADMIN")
                        // Permitir TODOs sobre ventas sin autenticación (temporal, pedido por usuario)
                        .requestMatchers(HttpMethod.POST, "/api/v1/sale/save/**").hasAnyRole("ADMIN")
                        // Permitir PATCH para CLIENTE o ADMIN (la lógica de permiso por propietario la maneja el servicio)
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/sale/**").hasAnyRole("CLIENTE", "ADMIN")
                        // DELETE lógico sobre ventas: requiere autenticación (CLIENTE o ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/sale/*/delete").hasAnyRole("CLIENTE", "ADMIN")
                        // Permitir temporalmente DELETE debug sin token
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/sale/debug-delete/**").permitAll()
                        // Permitir temporalmente RESTORE debug sin token
                        .requestMatchers("/api/v1/sale/debug-restore/**").permitAll()
                        // Permitir temporalmente PATCH restore sin token para pruebas: /api/v1/sale/{id}/restore
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/sale/*/restore").permitAll()
                        // Permitir temporalmente GET para el endpoint de prueba /api/v1/sale/delete-test/** (sin token)
                        .requestMatchers(HttpMethod.GET, "/api/v1/sale/delete-test/**").permitAll()
                        // Permitir temporalmente GET para listar mappings del controlador de ventas
                        .requestMatchers(HttpMethod.GET, "/api/v1/sale/_debug/mappings").permitAll()
                        // Permitir DELETE sobre ventas a CLIENTE o ADMIN (antes entraba en el matcher global que exige ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/sale/**").hasAnyRole("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/sale/summary").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/sale/all").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/sale/customer/**").hasAnyRole("ADMIN")

                        // ENDPOINTS DE USUARIOS: permitir que CLIENTE y ADMIN LISTEN usuarios/clientes
                        .requestMatchers(HttpMethod.GET, "/api/v1/user/listar").hasAnyRole("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/user/listar/**").hasAnyRole("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/user/listar/clientes").hasAnyRole("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/user/listar/ordenados").hasAnyRole("CLIENTE", "ADMIN")

                        // ADMIN tiene acceso GLOBAL a todo (DEBE IR AL FINAL)
                        .requestMatchers("/**").hasRole("ADMIN")

                        // Todos los demás endpoints requieren autenticación
                        .anyRequest().authenticated())
                // Configurar el manejo de excepciones de autenticación
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                // Agregar el filtro JWT antes del filtro de autenticación por defecto
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Bean para encriptar contraseñas usando BCrypt
     * Se usa para hashear contraseñas al registrar usuarios y validarlas al hacer
     * login
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Orígenes permitidos: aceptar cualquier puerto en localhost durante desarrollo
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // Aceptar explícitamente el header Authorization y otros headers habituales
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        // Exponer Authorization al cliente si lo necesita
        config.setExposedHeaders(List.of("Authorization"));
        // Permitir credenciales (cookies/creds). Ten en cuenta que con origen '*' esto
        // no
        // funcionaría en browsers, por eso definimos orígenes concretos arriba.
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Bean para el AuthenticationManager
     * Necesario para autenticar usuarios en el proceso de login
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Registrar un AuthenticationProvider (Dao) para que Spring Security pueda
     * construir un AuthenticationManager basado en el UserDetailsService.
     * Esto evita el log: "No authenticationProviders and no
     * parentAuthenticationManager defined. Returning null."
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(
            org.springframework.security.core.userdetails.UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}