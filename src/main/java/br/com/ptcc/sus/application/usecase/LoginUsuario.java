package br.com.ptcc.sus.application.usecase;

import br.com.ptcc.sus.application.port.*;
import br.com.ptcc.sus.domain.model.Usuario;

import java.util.Optional;

public class LoginUsuario {
    private final UsuarioPort usuarios;
    private final SenhaPort senhas;
    private final TokenPort tokens;

    public LoginUsuario(UsuarioPort u, SenhaPort s, TokenPort t) {
        usuarios = u;
        senhas = s;
        tokens = t;
    }

    public String executar(String email, String senha) {
        Optional<Usuario> u = usuarios.buscarPorEmail(email.trim().toLowerCase());
        if (u.isEmpty() || !senhas.corresponde(senha, u.get().senhaHash()))
            throw new IllegalArgumentException("E-mail ou senha inválidos.");
        return tokens.gerar(u.get().email());
    }
}