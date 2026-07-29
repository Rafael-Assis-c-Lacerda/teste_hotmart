package com.hotmart.controller;

import com.hotmart.dto.UsuarioLoginDTO;
import com.hotmart.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    // Injeta apenas o Service
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UsuarioLoginDTO dto) {
        try {
            // 1. O Controller pede pro Service fazer o trabalho pesado
            Map<String, String> tokens = authService.fazerLogin(dto.login(), dto.senha());
            
            // 2. O Controller cuida apenas de montar a estrutura Web (O Cookie de segurança)
            ResponseCookie cookie = ResponseCookie.from("refresh_token", tokens.get("refreshToken"))
                    .httpOnly(true)
                    .secure(true) 
                    .sameSite("None") 
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60) // 7 dias
                    .build();

            // 3. Devolve a resposta pronta
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(Map.of("token", tokens.get("accessToken")));

        } catch (RuntimeException e) {
            // Se o Service jogar um erro de senha errada, ele cai aqui
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(name = "refresh_token", required = false) String refreshToken) {
        System.out.println("==> BATEU NA ROTA DE REFRESH!");

        try {
            // 1. O Controller manda o Service tentar renovar o token
            String novoAccessToken = authService.renovarToken(refreshToken);
            
            System.out.println("==> SUCESSO: Novo Access Token gerado e devolvido para o React!");
            return ResponseEntity.ok(Map.of("token", novoAccessToken));

        } catch (IllegalArgumentException e) {
            System.out.println("==> ERRO: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            
        } catch (SecurityException e) {
            System.out.println("==> ERRO: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
            
        } catch (RuntimeException e) {
            System.out.println("==> ERRO: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // =======================================================
    // NOVA ROTA: DESTRUIR O COOKIE (LOGOUT)
    // =======================================================
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        System.out.println("==> BATEU NA ROTA DE LOGOUT!");
        
        // Cria um cookie com o mesmo nome, mas com tempo de vida 0 (Morte imediata)
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true) 
                .sameSite("None") 
                .path("/")
                .maxAge(0) 
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("mensagem", "Logout realizado com sucesso"));
    }
}