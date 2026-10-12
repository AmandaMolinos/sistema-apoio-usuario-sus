package br.com.ptcc.sus.infrastructure.persistence.repository;

import br.com.ptcc.sus.infrastructure.persistence.entity.VacinaJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VacinaJpaRepository extends JpaRepository<VacinaJpa, Long> {
    List<VacinaJpa> findAllByUsuarioIdOrderByDataAplicacaoDesc(Long usuarioId);
    Optional<VacinaJpa> findByIdAndUsuarioId(Long id, Long usuarioId);
}
