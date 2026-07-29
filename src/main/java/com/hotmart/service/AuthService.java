package com.hotmart.service;

import com.hotmart.config.JwtUtil;
import com.hotmart.model.Usuario;
import com.hotmart.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private final UsuarioRepository repository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository repository, JwtUtil jwtUtil) {
        this.repository = repository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // =======================================================
    // REGRA DE NEGÓCIO DO LOGIN
    // =======================================================
    public Map<String, String> fazerLogin(String login, String senha) {
        Optional<Usuario> usuarioOpt = repository.findByLogin(login);

        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Credenciais inválidas"); // Usuário não existe
        }

        Usuario u = usuarioOpt.get();

        if (!passwordEncoder.matches(senha, u.getSenha())) {
            throw new RuntimeException("Credenciais inválidas"); // Senha errada
        }

        // Se chegou aqui, a senha tá certa! Fabrica os tokens.
        String accessToken = jwtUtil.gerarToken(u.getLogin(), u.getRole().name());
        String refreshToken = jwtUtil.gerarRefreshToken(u.getLogin());

        // Devolve os dois tokens em um pacotinho (Map) para o Controller
        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        );
    }

    // =======================================================
    // REGRA DE NEGÓCIO DA RENOVAÇÃO DO TOKEN
    // =======================================================
    public String renovarToken(String refreshToken) {
        if (refreshToken == null) {
            throw new IllegalArgumentException("Refresh Token ausente no cookie");
        }

        if (!jwtUtil.isTokenValido(refreshToken)) {
            throw new SecurityException("Refresh Token inválido ou expirado");
        }

        String login = jwtUtil.obterLoginDoToken(refreshToken);

        Optional<Usuario> usuarioOpt = repository.findByLogin(login);
        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Usuário não encontrado");
        }

        Usuario u = usuarioOpt.get();
        
        // Fabrica e devolve apenas um crachá rápido novo
        return jwtUtil.gerarToken(u.getLogin(), u.getRole().name());
    }
}