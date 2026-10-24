package br.com.ptcc.sus.application.usecase;

import br.com.ptcc.sus.application.port.*;
import br.com.ptcc.sus.domain.model.*;

public class ConsultarPerfil {
    private final UsuarioPort usuarios;
    private final PerfilPort perfis;

    public ConsultarPerfil(UsuarioPort u, PerfilPort p) {
        usuarios = u;
        perfis = p;
    }

    public PerfilSaude executar(String email) {
        Usuario u = usuarios.buscarPorEmail(email).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        return perfis.buscarPorUsuarioId(u.id()).orElseThrow(() -> new IllegalArgumentException("Perfil não encontrado."));
    }
}