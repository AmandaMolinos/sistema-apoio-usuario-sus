package br.com.ptcc.sus.infrastructure.persistence.adapter;

import br.com.ptcc.sus.application.port.VacinaPort;
import br.com.ptcc.sus.domain.model.Vacina;
import br.com.ptcc.sus.infrastructure.persistence.entity.VacinaJpa;
import br.com.ptcc.sus.infrastructure.persistence.repository.VacinaJpaRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class VacinaAdapter implements VacinaPort {
    private final VacinaJpaRepository repo;

    public VacinaAdapter(VacinaJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Vacina salvar(Vacina vacina) {
        VacinaJpa entidade;
        if (vacina.id() == null) {
            entidade = new VacinaJpa(vacina.usuarioId(), vacina.nome(), vacina.dataAplicacao(),
                    vacina.lote(), vacina.localAplicacao());
        } else {
            entidade = repo.findByIdAndUsuarioId(vacina.id(), vacina.usuarioId())
                    .orElseThrow(() -> new IllegalArgumentException("Vacina não encontrada."));
            entidade.nome = vacina.nome();
            entidade.dataAplicacao = vacina.dataAplicacao();
            entidade.lote = vacina.lote();
            entidade.localAplicacao = vacina.localAplicacao();
        }
        return paraDominio(repo.save(entidade));
    }

    @Override
    public List<Vacina> listarPorUsuarioId(Long usuarioId) {
        return repo.findAllByUsuarioIdOrderByDataAplicacaoDesc(usuarioId).stream().map(this::paraDominio).toList();
    }

    @Override
    public Optional<Vacina> buscarPorIdEUsuarioId(Long id, Long usuarioId) {
        return repo.findByIdAndUsuarioId(id, usuarioId).map(this::paraDominio);
    }

    @Override
    public void excluir(Long id) {
        repo.deleteById(id);
    }

    private Vacina paraDominio(VacinaJpa v) {
        return new Vacina(v.id, v.usuarioId, v.nome, v.dataAplicacao, v.lote, v.localAplicacao);
    }
}
