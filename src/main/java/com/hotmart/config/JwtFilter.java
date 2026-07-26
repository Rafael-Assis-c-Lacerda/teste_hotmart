package com.hotmart.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // O React vai mandar o token no cabeçalho "Authorization: Bearer <token>"
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Tira a palavra "Bearer "

            if (jwtUtil.isTokenValido(token)) {
                String login = jwtUtil.extrairLogin(token);
                String role = jwtUtil.extrairRole(token);

                // Traduz a sua Role (ADMIN, GESTOR) para o formato do Spring Security (ROLE_ADMIN)
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        login, null, Collections.singletonList(authority)
                );

                // Coloca o "crachá" aprovado no contexto da aplicação
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        // Deixa a requisição seguir o seu caminho até o Controller
        filterChain.doFilter(request, response);
    }
}