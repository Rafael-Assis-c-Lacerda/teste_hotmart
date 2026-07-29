package com.hotmart.controller;

import com.hotmart.config.JwtUtil;
import com.hotmart.dto.UsuarioLoginDTO;
import com.hotmart.model.Usuario;
import com.hotmart.repository.UsuarioRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
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
        Optional<Usuario> usuarioOpt = repository.findByLogin(dto.login());

        if (usuarioOpt.isPresent()) {
            Usuario u = usuarioOpt.get();
            
            if (passwordEncoder.matches(dto.senha(), u.getSenha())) {
                
                String accessToken = jwtUtil.gerarToken(u.getLogin(), u.getRole().name());
                String refreshToken = jwtUtil.gerarRefreshToken(u.getLogin());
                
                // Monta o Cookie preparado para contornar o bloqueio do Chrome (SameSite=None)
                ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                        .httpOnly(true)
                        .secure(true) // OBRIGATÓRIO ser true quando usa SameSite None
                        .sameSite("None") // LIBERA o envio entre portas diferentes (5173 -> 8080)
                        .path("/")
                        .maxAge(7 * 24 * 60 * 60) // 7 dias
                        .build();

                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, cookie.toString())
                        .body(Map.of("token", accessToken));
            }
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais inválidas");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(name = "refresh_token", required = false) String refreshToken) {
        
        System.out.println("==> BATEU NA ROTA DE REFRESH!");

        // 1. Verifica se o navegador mandou o cookie (Erro 400)
        if (refreshToken == null) {
            System.out.println("==> ERRO: O cookie não chegou no Java!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Refresh Token ausente no cookie");
        }

        // 2. Verifica se o token é válido (Erro 401)
        if (!jwtUtil.isTokenValido(refreshToken)) {
            System.out.println("==> ERRO: O Token é inválido ou expirou!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh Token inválido ou expirado");
        }

        // 3. Extrai o usuário
        String login = jwtUtil.obterLoginDoToken(refreshToken);

        // 4. Busca no banco (Erro 404)
        Optional<Usuario> usuarioOpt = repository.findByLogin(login);
        if (usuarioOpt.isEmpty()) {
            System.out.println("==> ERRO: Usuário não existe mais no banco!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado");
        }

        Usuario u = usuarioOpt.get();
        
        // 5. Sucesso! (200 OK)
        String novoAccessToken = jwtUtil.gerarToken(u.getLogin(), u.getRole().name());
        System.out.println("==> SUCESSO: Novo Access Token gerado e devolvido para o React!");

        return ResponseEntity.ok(Map.of("token", novoAccessToken));
    }
}