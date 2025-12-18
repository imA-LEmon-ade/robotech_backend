package com.robotech.robotech_backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import com.robotech.robotech_backend.model.Usuario;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET_KEY =
            "ESTA_ES_MI_SECRET_KEY_SUPER_SECRETA_PARA_JWT_256_BITS_1234567890";

    private static final long EXPIRATION_MS = 1000 * 60 * 60 * 4; // 4 horas


    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }


    // 🔥 GENERAR TOKEN
    public String generarToken(Usuario usuario) {
        return Jwts.builder()
                .setSubject(usuario.getCorreo())
                .claim("rol", usuario.getRol())
                .claim("idUsuario", usuario.getIdUsuario())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    // 🔥 VALIDAR TOKEN
    public boolean esTokenValido(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            System.out.println("Token inválido: " + e.getMessage());
            return false;
        }
    }


    // 🔥 EXTRAER CORREO
    public String obtenerCorreo(String token) {
        return obtenerClaims(token).getSubject();
    }


    private Claims obtenerClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
