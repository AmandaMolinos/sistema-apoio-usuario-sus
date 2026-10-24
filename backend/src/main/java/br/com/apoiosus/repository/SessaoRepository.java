package br.com.apoiosus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.apoiosus.model.entity.Sessao;

public interface SessaoRepository extends JpaRepository<Sessao, Integer> {

    Optional<Sessao> findByTokenHash(String tokenHash);
}
