package br.com.ptcc.sus.application.usecase;

import br.com.ptcc.sus.application.port.UsuarioPort;
import br.com.ptcc.sus.application.port.VacinaPort;

public class ExcluirVacina {
    private final UsuarioPort usuarios;
    private final VacinaPort vacinas;

    public ExcluirVacina(UsuarioPort usuarios, VacinaPort vacinas) {
        this.usuarios = usuarios;
        this.vacinas = vacinas;
    }

    public void executar(String email, Long id) {
        var usuario = usuarios.buscarPorEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        vacinas.buscarPorIdEUsuarioId(id, usuario.id())
                .orElseThrow(() -> new IllegalArgumentException("Vacina não encontrada."));
        vacinas.excluir(id);
    }
}
