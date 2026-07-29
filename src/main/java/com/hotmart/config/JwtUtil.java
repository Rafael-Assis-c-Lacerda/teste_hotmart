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
    
    private final long validade = 10000; // 1 hora de validade
    private final long validadeRefreshToken = 604800000L; // 7 dias de validade (em milissegundos)

    // MÉTODO ORIGINAL: Gera o token normal de 1h
    public String gerarToken(String login, String role) {
        return Jwts.builder()
                .setSubject(login)
                .claim("role", role) // Guarda a permissão dentro do token
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + validade))
                .signWith(key)
                .compact();
    }

    // NOVO MÉTODO: Gera o token de 7 dias (usado apenas para criar o cookie de renovação)
    public String gerarRefreshToken(String login) {
        return Jwts.builder()
                .setSubject(login)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + validadeRefreshToken))
                .signWith(key)
                .compact();
    }

    public String extrairLogin(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    // NOVO MÉTODO: Apenas um "apelido" que o AuthController estava procurando
    public String obterLoginDoToken(String token) {
        return extrairLogin(token);
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