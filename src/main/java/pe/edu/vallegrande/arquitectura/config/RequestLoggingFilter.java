package pe.edu.vallegrande.arquitectura.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration;

@Component
@Order(1)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String method = request.getMethod();
            String uri = request.getRequestURI();
            String query = request.getQueryString();
            String remote = request.getRemoteAddr();
            String auth = request.getHeader("Authorization");

            LOGGER.info("Incoming request: method={} uri={} query={} remote={} AuthorizationPresent={}",
                    method, uri, query, remote, (auth != null && !auth.isBlank()));

            // Opcional: listar algunos headers (sin revelar token completo)
            StringBuilder headers = new StringBuilder();
            Enumeration<String> names = request.getHeaderNames();
            while (names != null && names.hasMoreElements()) {
                String name = names.nextElement();
                String value = request.getHeader(name);
                if ("authorization" .equalsIgnoreCase(name) && value != null) {
                    // no loguear el token completo, solo indicar que existe y longitud
                    headers.append(name).append("=<hidden,length=").append(value.length()).append("> ");
                } else {
                    headers.append(name).append("=").append(value).append(" ");
                }
            }
            LOGGER.debug("Request headers: {}", headers.toString());

        } catch (Exception ex) {
            LOGGER.warn("Request logging failed: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
