package pe.com.libertyfinance.ux_ms_apigateway.filters;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.servlet.HandlerInterceptor;
import pe.com.libertyfinance.ux_ms_apigateway.models.SessionData;
import pe.com.libertyfinance.ux_ms_apigateway.services.JwtService;

import java.util.List;

@Component
public class SessionValidatorInterceptor
        implements HandlerInterceptor {

    private final RestClient restClient;
    private final JwtService jwtService;

    private static final List<String> PUBLIC_PATHS = List.of("/auth");

    public SessionValidatorInterceptor(
            RestClient.Builder builder,
            JwtService jwtService
    ) {
        this.jwtService = jwtService;
        this.restClient = builder
                .baseUrl("${MS_SESSION_URL:http://localhost:8083}")
                .build();
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {

        String path = request.getRequestURI();

        // 1. si es ruta pública, deja pasar
        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            return true;
        }

        // 2. extrae el JWT
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        String jwt = authHeader.substring(7);

        // 3. valida JWT localmente
        if (!jwtService.isValid(jwt)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 4. extrae sessionId del JWT
        String sessionId = jwtService.extractSessionId(jwt);

        // 5. consulta MS-Session → Redis
        SessionData sessionData = restClient.get()
                .uri("/session/{sessionId}", sessionId)
                .retrieve()
                .body(SessionData.class);

        if (sessionData == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 6. inyecta datos en el request para propagar a MS internos
        request.setAttribute("X-User-Id", sessionData.userId());
        request.setAttribute("X-User-Role", sessionData.role());
        request.setAttribute("X-Person-Id", sessionData.personId());

        return true;
    }
}