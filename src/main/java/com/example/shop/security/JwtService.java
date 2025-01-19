package com.example.shop.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    // W realnej aplikacji klucz lepiej trzymać w .env lub w application.properties
    // i zapewnić, że ma on długość co najmniej 32 znaków (dla HS256).
    private static final String SECRET_KEY = "bardzotrudnehaslodlaJWTbardzotrudnehaslo";

    /**
     * Generuje token JWT na podstawie nazwy użytkownika.
     * Przykładowo ważny 24h (możesz dostosować wedle potrzeb).
     */
    public String generateToken(String username) {
        Key key = getSigningKey();
        long now = System.currentTimeMillis();
        long expiration = now + 1000 * 60 * 60 * 24; // 24h

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Wyciąga nazwę użytkownika (subject) z tokenu.
     */
    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    /**
     * Sprawdza, czy token jest ważny (nieprzeterminowany) i czy należy do danego użytkownika.
     */
    public boolean isTokenValid(String token, String username) {
        final String userNameFromToken = extractUsername(token);
        return (userNameFromToken.equals(username) && !isTokenExpired(token));
    }

    /**
     * Sprawdza, czy token wygasł (data wygaśnięcia jest w przeszłości).
     */
    private boolean isTokenExpired(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        return expiration.before(new Date());
    }

    /**
     * Parsuje i wyciąga wszystkie dane (Claims) z tokenu.
     */
    private Claims extractAllClaims(String token) {
        Key key = getSigningKey();
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Tworzy obiekt typu Key na podstawie łańcucha SECRET_KEY.
     * Dla HS256 potrzebne jest przynajmniej 32 bajty entropii.
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }
}
