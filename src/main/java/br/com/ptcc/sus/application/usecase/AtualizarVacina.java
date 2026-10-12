package br.com.ptcc.sus.application.usecase;

import br.com.ptcc.sus.application.port.UsuarioPort;
import br.com.ptcc.sus.application.port.VacinaPort;
import br.com.ptcc.sus.domain.model.Vacina;
import java.time.LocalDate;

public class AtualizarVacina {
    private final UsuarioPort usuarios;
    private final VacinaPort vacinas;

    public AtualizarVacina(UsuarioPort usuarios, VacinaPort vacinas) {
        this.usuarios = usuarios;
        this.vacinas = vacinas;
    }

    public Vacina executar(String email, Long id, String nome, LocalDate dataAplicacao, String lote, String localAplicacao) {
        var usuario = usuarios.buscarPorEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        Vacina vacina = vacinas.buscarPorIdEUsuarioId(id, usuario.id())
                .orElseThrow(() -> new IllegalArgumentException("Vacina não encontrada."));
        vacina.atualizar(nome, dataAplicacao, lote, localAplicacao);
        return vacinas.salvar(vacina);
    }
}
