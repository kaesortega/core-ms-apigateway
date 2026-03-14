package pe.com.libertyfinance.ux_ms_apigateway.models;

public record SessionData(
        String userId,
        String role,
        String personId
) {
}
