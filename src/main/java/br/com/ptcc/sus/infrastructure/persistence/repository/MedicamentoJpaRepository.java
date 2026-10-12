package br.com.ptcc.sus.infrastructure.persistence.repository;

import br.com.ptcc.sus.infrastructure.persistence.entity.MedicamentoJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MedicamentoJpaRepository extends JpaRepository<MedicamentoJpa, Long> {
    List<MedicamentoJpa> findAllByUsuarioIdOrderByDataInicioDesc(Long usuarioId);
    Optional<MedicamentoJpa> findByIdAndUsuarioId(Long id, Long usuarioId);
}
