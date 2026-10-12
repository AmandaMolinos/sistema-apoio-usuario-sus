package br.com.ptcc.sus.infrastructure.persistence.repository;

import br.com.ptcc.sus.infrastructure.persistence.entity.PerfilJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerfilJpaRepository extends JpaRepository<PerfilJpa, Long> {
    Optional<PerfilJpa> findByUsuarioId(Long usuarioId);
}