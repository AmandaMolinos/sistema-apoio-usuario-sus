package br.com.apoiosus.dto.response;

import java.time.LocalDate;

import br.com.apoiosus.model.entity.Usuario;

public record UsuarioResponse(Integer id, String nome, String email, LocalDate dataNascimento) {

    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getDataNascimento());
    }
}

