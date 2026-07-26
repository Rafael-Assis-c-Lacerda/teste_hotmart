package com.hotmart.controller;

import com.hotmart.dto.UsuarioResponseDTO;
import com.hotmart.model.Usuario;
import com.hotmart.repository.UsuarioRepository;
import com.hotmart.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService service;
    private final UsuarioRepository repository;

    public UsuarioController(UsuarioService service, UsuarioRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    // =======================================================
    // 1. CADASTRAR (POST)
    // =======================================================
    @PostMapping
    public ResponseEntity<?> cadastrarUsuario(@RequestBody Usuario usuario) {
        try {
            Usuario usuarioSalvo = service.cadastrar(usuario);
            
            UsuarioResponseDTO dto = new UsuarioResponseDTO(
                    usuarioSalvo.getId(),
                    usuarioSalvo.getNome(),
                    usuarioSalvo.getEmail(),
                    usuarioSalvo.getLogin(),
                    usuarioSalvo.getRole()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // =======================================================
    // 2. LISTAR TODOS (GET)
    // =======================================================
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        List<UsuarioResponseDTO> lista = repository.findAll().stream()
                .map(u -> new UsuarioResponseDTO(u.getId(), u.getNome(), u.getEmail(), u.getLogin(), u.getRole()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(lista);
    }

    // =======================================================
    // 3. BUSCAR POR ID (GET)
    // =======================================================
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        Optional<Usuario> usuario = repository.findById(id);

        if (usuario.isPresent()) {
            Usuario u = usuario.get();
            UsuarioResponseDTO dto = new UsuarioResponseDTO(u.getId(), u.getNome(), u.getEmail(), u.getLogin(), u.getRole());
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado!");
        }
    }

    // =======================================================
    // 4. DELETAR (DELETE)
    // =======================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarUsuario(@PathVariable Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.ok("Usuário deletado com sucesso!");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado!");
        }
    }

    // =======================================================
    // 5. ATUALIZAR (PUT)
    // =======================================================
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarUsuario(@PathVariable Long id, @RequestBody Usuario dadosAtualizados) {
        Optional<Usuario> usuarioExistente = repository.findById(id);

        if (usuarioExistente.isPresent()) {
            Usuario u = usuarioExistente.get();
            
            u.setNome(dadosAtualizados.getNome());
            u.setEmail(dadosAtualizados.getEmail());
            
            repository.save(u);
            
            UsuarioResponseDTO dto = new UsuarioResponseDTO(u.getId(), u.getNome(), u.getEmail(), u.getLogin(), u.getRole());
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado!");
        }
    }
    // =======================================================
    // VER MEUS DADOS (GET) - Qualquer usuário logado acessa
    // =======================================================
    @GetMapping("/meus-dados")
    public ResponseEntity<?> verMeusDados() {
        // Magia do Spring: Pega o login de quem está fazendo a requisição naquele exato momento!
        String loginLogado = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        
        Optional<Usuario> usuario = repository.findByLogin(loginLogado);
        
        if (usuario.isPresent()) {
            Usuario u = usuario.get();
            return ResponseEntity.ok(new UsuarioResponseDTO(u.getId(), u.getNome(), u.getEmail(), u.getLogin(), u.getRole()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado!");
    }

    // =======================================================
    // ATUALIZAR MEUS DADOS (PUT) - Qualquer usuário logado acessa
    // =======================================================
    @PutMapping("/meus-dados")
    public ResponseEntity<?> atualizarMeusDados(@RequestBody Usuario dadosAtualizados) {
        String loginLogado = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        
        Optional<Usuario> usuarioExistente = repository.findByLogin(loginLogado);

        if (usuarioExistente.isPresent()) {
            Usuario u = usuarioExistente.get();
            
            // Atualiza SOMENTE o que é seguro. A Role (ADMIN/USUARIO) fica intacta!
            u.setNome(dadosAtualizados.getNome());
            u.setEmail(dadosAtualizados.getEmail());
            
            repository.save(u);
            return ResponseEntity.ok(new UsuarioResponseDTO(u.getId(), u.getNome(), u.getEmail(), u.getLogin(), u.getRole()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado!");
    }
}