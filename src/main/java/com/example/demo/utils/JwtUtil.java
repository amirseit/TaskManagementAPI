package com.example.demo.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    // Get the secret key from the environment variable SECURE_SECRET_KEY
    private final String SECRET = System.getenv("SECURE_SECRET_KEY") != null
            ? System.getenv("SECURE_SECRET_KEY")
            : "sMmHaTcmQM7cNX7p6+p7qCWAF5bYBgxFp0rK5M1TzrI="; // Default key for local development
    private final Key SECRET_KEY = new SecretKeySpec(
            Base64.getDecoder().decode(SECRET),
            SignatureAlgorithm.HS256.getJcaName()
    );

    // Initialize the JwtParser instance
    private final JwtParser jwtParser;

    public JwtUtil() {
        JwtParserBuilder builder = Jwts.parser(); // Use the builder pattern
        this.jwtParser = builder.setSigningKey(SECRET_KEY).build(); // Build the parser with the secret key
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return jwtParser.parseClaimsJws(token).getBody(); // Use the pre-built parser
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // Token validity: 10 hours
                .signWith(SECRET_KEY) // Automatically infers the algorithm from the key
                .compact();
    }

    public boolean isTokenValid(String token, String username) {
        return username.equals(extractUsername(token)) && !isTokenExpired(token);
    }

    public boolean validateToken(String token, org.springframework.security.core.userdetails.UserDetails userDetails) {
        final String username = extractUsername(token); // Extract the username from the token
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token)); // Check username match and token expiry
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}
