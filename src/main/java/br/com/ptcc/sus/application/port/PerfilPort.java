package br.com.ptcc.sus.application.port;

import br.com.ptcc.sus.domain.model.PerfilSaude;

import java.util.Optional;

public interface PerfilPort {
    PerfilSaude salvar(PerfilSaude perfil);

    Optional<PerfilSaude> buscarPorUsuarioId(Long usuarioId);
}