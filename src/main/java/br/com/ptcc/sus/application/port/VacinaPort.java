package br.com.ptcc.sus.application.port;

import br.com.ptcc.sus.domain.model.Vacina;
import java.util.List;
import java.util.Optional;

public interface VacinaPort {
    Vacina salvar(Vacina vacina);
    List<Vacina> listarPorUsuarioId(Long usuarioId);
    Optional<Vacina> buscarPorIdEUsuarioId(Long id, Long usuarioId);
    void excluir(Long id);
}
