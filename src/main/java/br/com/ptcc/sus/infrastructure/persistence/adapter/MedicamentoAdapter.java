package br.com.ptcc.sus.infrastructure.persistence.adapter;

import br.com.ptcc.sus.application.port.MedicamentoPort;
import br.com.ptcc.sus.domain.model.Medicamento;
import br.com.ptcc.sus.infrastructure.persistence.entity.MedicamentoJpa;
import br.com.ptcc.sus.infrastructure.persistence.repository.MedicamentoJpaRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class MedicamentoAdapter implements MedicamentoPort {
    private final MedicamentoJpaRepository repo;

    public MedicamentoAdapter(MedicamentoJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Medicamento salvar(Medicamento medicamento) {
        MedicamentoJpa entidade;
        if (medicamento.id() == null) {
            entidade = new MedicamentoJpa(medicamento.usuarioId(), medicamento.nome(), medicamento.dosagem(),
                    medicamento.frequencia(), medicamento.dataInicio(), medicamento.dataFim());
        } else {
            entidade = repo.findByIdAndUsuarioId(medicamento.id(), medicamento.usuarioId())
                    .orElseThrow(() -> new IllegalArgumentException("Medicamento não encontrado."));
            entidade.nome = medicamento.nome();
            entidade.dosagem = medicamento.dosagem();
            entidade.frequencia = medicamento.frequencia();
            entidade.dataInicio = medicamento.dataInicio();
            entidade.dataFim = medicamento.dataFim();
        }
        return paraDominio(repo.save(entidade));
    }

    @Override
    public List<Medicamento> listarPorUsuarioId(Long usuarioId) {
        return repo.findAllByUsuarioIdOrderByDataInicioDesc(usuarioId).stream().map(this::paraDominio).toList();
    }

    @Override
    public Optional<Medicamento> buscarPorIdEUsuarioId(Long id, Long usuarioId) {
        return repo.findByIdAndUsuarioId(id, usuarioId).map(this::paraDominio);
    }

    @Override
    public void excluir(Long id) {
        repo.deleteById(id);
    }

    private Medicamento paraDominio(MedicamentoJpa m) {
        return new Medicamento(m.id, m.usuarioId, m.nome, m.dosagem, m.frequencia, m.dataInicio, m.dataFim);
    }
}
