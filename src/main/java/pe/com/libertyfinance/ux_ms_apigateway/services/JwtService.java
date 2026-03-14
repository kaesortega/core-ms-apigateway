package pe.com.libertyfinance.ux_ms_apigateway.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // valida que el token sea válido y no esté expirado
    public boolean isValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            // token expirado
            return false;
        } catch (MalformedJwtException e) {
            // token mal formado
            return false;
        } catch (SignatureException e) {
            // firma inválida (secret diferente)
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractSessionId(String token) {
        return getClaims(token).get("sessionId", String.class);
    }

    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public String extractScope(String token) {
        return getClaims(token).get("scope", String.class);
    }

    public String extractUserId(String token) {
        return getClaims(token).getSubject();
    }

    // método privado central para parsear el token
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}