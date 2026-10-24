package br.com.ptcc.sus.application.usecase;

import br.com.ptcc.sus.application.port.*;
import br.com.ptcc.sus.domain.model.*;

public class AtualizarPerfil {
    private final UsuarioPort usuarios;
    private final PerfilPort perfis;

    public AtualizarPerfil(UsuarioPort u, PerfilPort p) {
        usuarios = u;
        perfis = p;
    }

    public PerfilSaude executar(String email, Double peso, Double altura, String tipo, String alergias, String observacoes) {
        Usuario u = usuarios.buscarPorEmail(email).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        PerfilSaude p = perfis.buscarPorUsuarioId(u.id()).orElse(new PerfilSaude(null, u.id(), null, null, null, null, null));
        p.atualizar(peso, altura, tipo, alergias, observacoes);
        return perfis.salvar(p);
    }
}