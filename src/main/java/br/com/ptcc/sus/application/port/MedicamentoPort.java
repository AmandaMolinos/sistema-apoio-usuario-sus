package br.com.ptcc.sus.application.port;

import br.com.ptcc.sus.domain.model.Medicamento;
import java.util.List;
import java.util.Optional;

public interface MedicamentoPort {
    Medicamento salvar(Medicamento medicamento);
    List<Medicamento> listarPorUsuarioId(Long usuarioId);
    Optional<Medicamento> buscarPorIdEUsuarioId(Long id, Long usuarioId);
    void excluir(Long id);
}
