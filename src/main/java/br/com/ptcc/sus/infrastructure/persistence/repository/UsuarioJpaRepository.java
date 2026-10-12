package br.com.ptcc.sus.infrastructure.persistence.repository;

import br.com.ptcc.sus.infrastructure.persistence.entity.UsuarioJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioJpa, Long> {
    Optional<UsuarioJpa> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}