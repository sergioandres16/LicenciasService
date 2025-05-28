package org.saeta.licenciasservice.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.saeta.licenciasservice.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret:LicenciasSecretKey123}")
    private String secretKey;

    // 1 hora de expiración
    private static final long EXPIRATION_TIME = 3600000L;

    /**
     * Genera un token JWT para el usuario
     */
    public String generateToken(User user) {
        return JWT.create()
                .withSubject(user.getUsername())
                .withClaim("userId", user.getId())
                .withClaim("email", user.getEmail())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC256(secretKey));
    }

    /**
     * Valida el token y retorna true/false
     */
    public boolean validateToken(String token) {
        try {
            DecodedJWT decoded = JWT.require(Algorithm.HMAC256(secretKey))
                    .build()
                    .verify(token);
            return !decoded.getExpiresAt().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtiene el username del token
     */
    public String getUsernameFromToken(String token) {
        DecodedJWT decoded = JWT.decode(token);
        return decoded.getSubject();
    }
}