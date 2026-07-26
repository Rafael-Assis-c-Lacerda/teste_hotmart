package com.hotmart.service;

import com.hotmart.model.Usuario;
import com.hotmart.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final BCryptPasswordEncoder passwordEncoder;

    // O Spring vê esse construtor e injeta o repositório sozinho (adeus "new DAO()")
    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

   public Usuario cadastrar(Usuario novoUsuario) {
        Optional<Usuario> usuarioExistente = repository.findByLogin(novoUsuario.getLogin());
        
        if (usuarioExistente.isPresent()) {
            throw new RuntimeException("Já existe um usuário cadastrado com este login!");
        }

        // TRAVA DE SEGURANÇA: Todo cadastro público vira USUARIO comum obrigatoriamente.
        novoUsuario.setRole(com.hotmart.model.Role.USUARIO); 

        String senhaCriptografada = passwordEncoder.encode(novoUsuario.getSenha());
        novoUsuario.setSenha(senhaCriptografada);

        return repository.save(novoUsuario);
    }
}