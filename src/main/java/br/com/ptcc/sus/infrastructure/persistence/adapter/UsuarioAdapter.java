package br.com.ptcc.sus.infrastructure.persistence.adapter;

import br.com.ptcc.sus.application.port.UsuarioPort;
import br.com.ptcc.sus.domain.model.Usuario;
import br.com.ptcc.sus.infrastructure.persistence.entity.UsuarioJpa;
import br.com.ptcc.sus.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UsuarioAdapter implements UsuarioPort {
    private final UsuarioJpaRepository repo;

    public UsuarioAdapter(UsuarioJpaRepository r) {
        repo = r;
    }

    public boolean emailExiste(String e) {
        return repo.existsByEmailIgnoreCase(e);
    }

    public Usuario salvar(Usuario u) {
        UsuarioJpa j = repo.save(new UsuarioJpa(u.nome(), u.email(), u.senhaHash(), u.dataNascimento()));
        return toDomain(j);
    }

    public Optional<Usuario> buscarPorEmail(String e) {
        return repo.findByEmailIgnoreCase(e).map(this::toDomain);
    }

    private Usuario toDomain(UsuarioJpa j) {
        return new Usuario(j.id, j.nome, j.email, j.senha, j.dataNascimento);
    }
}