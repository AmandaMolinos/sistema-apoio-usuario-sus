package br.com.ptcc.sus.infrastructure.persistence.adapter;

import br.com.ptcc.sus.application.port.PerfilPort;
import br.com.ptcc.sus.domain.model.PerfilSaude;
import br.com.ptcc.sus.infrastructure.persistence.entity.PerfilJpa;
import br.com.ptcc.sus.infrastructure.persistence.repository.PerfilJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PerfilAdapter implements PerfilPort {
    private final PerfilJpaRepository repo;

    public PerfilAdapter(PerfilJpaRepository r) {
        repo = r;
    }

    public PerfilSaude salvar(PerfilSaude p) {
        PerfilJpa j = repo.findByUsuarioId(p.usuarioId()).orElse(new PerfilJpa(p.usuarioId(), p.peso(), p.altura(), p.tipoSanguineo(), p.alergias(), p.observacoesMedicas()));
        j.peso = p.peso();
        j.altura = p.altura();
        j.tipoSanguineo = p.tipoSanguineo();
        j.alergias = p.alergias();
        j.observacoesMedicas = p.observacoesMedicas();
        j = repo.save(j);
        return toDomain(j);
    }

    public Optional<PerfilSaude> buscarPorUsuarioId(Long id) {
        return repo.findByUsuarioId(id).map(this::toDomain);
    }

    private PerfilSaude toDomain(PerfilJpa j) {
        return new PerfilSaude(j.id, j.usuarioId, j.peso, j.altura, j.tipoSanguineo, j.alergias, j.observacoesMedicas);
    }
}