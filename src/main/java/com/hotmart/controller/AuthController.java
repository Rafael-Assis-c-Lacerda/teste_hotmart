package com.hotmart.controller;

import com.hotmart.config.JwtUtil;
import com.hotmart.dto.UsuarioLoginDTO;
import com.hotmart.model.Usuario;
import com.hotmart.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioRepository repository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UsuarioRepository repository, JwtUtil jwtUtil) {
        this.repository = repository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UsuarioLoginDTO dto) {
        // 1. Busca o usuário no banco
        Optional<Usuario> usuarioOpt = repository.findByLogin(dto.login());

        if (usuarioOpt.isPresent()) {
            Usuario u = usuarioOpt.get();
            
            // 2. Compara a senha em texto puro que veio do Front com o Hash do banco
            if (passwordEncoder.matches(dto.senha(), u.getSenha())) {
                
                // 3. Se bateu, gera o crachá (Token) guardando o Login e a Role
                String token = jwtUtil.gerarToken(u.getLogin(), u.getRole().name());
                
                // Devolve o token em formato JSON
                return ResponseEntity.ok("{\"token\": \"" + token + "\"}");
            }
        }
        
        // Se a senha estiver errada ou usuário não existir, devolve 401 Unauthorized
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais inválidas");
    }
}