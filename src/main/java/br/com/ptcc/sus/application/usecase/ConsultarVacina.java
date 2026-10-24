package br.com.ptcc.sus.application.usecase;

import br.com.ptcc.sus.application.port.UsuarioPort;
import br.com.ptcc.sus.application.port.VacinaPort;
import br.com.ptcc.sus.domain.model.Vacina;

public class ConsultarVacina {
    private final UsuarioPort usuarios;
    private final VacinaPort vacinas;

    public ConsultarVacina(UsuarioPort usuarios, VacinaPort vacinas) {
        this.usuarios = usuarios;
        this.vacinas = vacinas;
    }

    public Vacina executar(String email, Long id) {
        var usuario = usuarios.buscarPorEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        return vacinas.buscarPorIdEUsuarioId(id, usuario.id())
                .orElseThrow(() -> new IllegalArgumentException("Vacina não encontrada."));
    }
}
