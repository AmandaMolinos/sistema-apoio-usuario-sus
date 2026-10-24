package br.com.apoiosus.dto.response;

import br.com.apoiosus.model.Usuario;

// Não inclui a senha: só os campos que podem sair da API.
public record UsuarioResponse(int id, String nome, String email, String dataNascimento) {

    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.id(), u.nome(), u.email(), u.dataNascimento());
    }
}
