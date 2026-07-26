package com.hotmart.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    
    // Chave secreta fixa para os testes (na vida real isso fica escondido nas variáveis de ambiente)
    private final SecretKey key = Keys.hmacShaKeyFor("ChaveSecretaDaHotmartSuperSegura2026".getBytes());
    private final long validade = 3600000; // 1 hora de validade

    public String gerarToken(String login, String role) {
        return Jwts.builder()
                .setSubject(login)
                .claim("role", role) // Guarda a permissão dentro do token
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + validade))
                .signWith(key)
                .compact();
    }

    public String extrairLogin(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    public String extrairRole(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get("role", String.class);
    }

    public boolean isTokenValido(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}