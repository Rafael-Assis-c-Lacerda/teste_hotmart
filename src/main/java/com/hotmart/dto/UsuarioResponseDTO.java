package com.hotmart.dto;

import com.hotmart.model.Role;

// Repare: a senha NÃO existe aqui. Acabou a gambiarra de "setSenha("")"!
public record UsuarioResponseDTO(Long id, String nome, String email, String login, Role role) {
}