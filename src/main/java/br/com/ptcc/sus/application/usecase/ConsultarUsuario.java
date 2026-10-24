package br.com.ptcc.sus.application.usecase;

import br.com.ptcc.sus.application.port.UsuarioPort;
import br.com.ptcc.sus.domain.model.Usuario;

public class ConsultarUsuario {
    private final UsuarioPort usuarios;

    public ConsultarUsuario(UsuarioPort u) {
        usuarios = u;
    }

    public Usuario executar(String email) {
        return usuarios.buscarPorEmail(email).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }
}