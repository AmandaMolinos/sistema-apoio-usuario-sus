package br.com.ptcc.sus.application.port;

import br.com.ptcc.sus.domain.model.Usuario;

import java.util.Optional;

public interface UsuarioPort {
    boolean emailExiste(String email);

    Usuario salvar(Usuario usuario);

    Optional<Usuario> buscarPorEmail(String email);
}