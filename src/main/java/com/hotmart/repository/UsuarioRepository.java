package com.hotmart.repository;

import com.hotmart.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Só de escrever isso, o Spring já cria o "SELECT * FROM usuarios WHERE login = ?"
    Optional<Usuario> findByLogin(String login);
    
}