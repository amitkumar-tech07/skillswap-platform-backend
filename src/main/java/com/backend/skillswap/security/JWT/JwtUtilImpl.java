package com.backend.skillswap.security.JWT;

import com.backend.skillswap.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;

@Service
public class JwtUtilImpl implements JwtUtil {

    // Secret key from application.yml
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    // Access token expiry from config
    @Value("${jwt.access-expiry}")
    private Duration accessTokenExpiry;

    // Create signing key from secret
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    // ================= GENERATE ACCESS TOKEN =================
    @Override
    public String generateAccessToken(UserEntity user) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiry.toMillis());

        return Jwts.builder()
                // JWT subject = email (matches CustomUserDetails.getUsername)
                .setSubject(user.getEmail())

                // Custom claims
                .claim("userId", user.getId())

                // MULTIPLE ROLES → ["USER", "PROVIDER"]
                .claim(
                        "roles",
                        user.getRoles()
                                .stream()
                                .map(Enum::name)   // Role.USER → "USER"
                                .toList()
                )

                // Token timestamps
                .setIssuedAt(now)
                .setExpiration(expiryDate)

                // Sign token
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ================= EXTRACT USERNAME =================
    @Override
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // ================= TOKEN VALIDATION =================
    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    // ================= EXPIRY CHECK =================
    @Override
    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    // ================= EXTRACT USER ID FROM TOKEN =================
    @Override
    public Long getUserIdFromToken(String token) {
        Number id = extractAllClaims(token).get("userId", Number.class);
        return id != null ? id.longValue() : null;
    }

    // ================= EXPIRY FOR RESPONSE =================
    @Override
    public long getAccessTokenExpiryInMs() {
        return accessTokenExpiry.toMillis();
    }

    // ================= COMMON PARSE CLAIMS =================
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}



