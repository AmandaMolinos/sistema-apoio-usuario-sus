package br.com.ptcc.sus.application.usecase;

import br.com.ptcc.sus.application.port.UsuarioPort;
import br.com.ptcc.sus.application.port.VacinaPort;
import br.com.ptcc.sus.domain.model.Vacina;
import java.util.List;

public class ListarVacinas {
    private final UsuarioPort usuarios;
    private final VacinaPort vacinas;

    public ListarVacinas(UsuarioPort usuarios, VacinaPort vacinas) {
        this.usuarios = usuarios;
        this.vacinas = vacinas;
    }

    public List<Vacina> executar(String email) {
        var usuario = usuarios.buscarPorEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        return vacinas.listarPorUsuarioId(usuario.id());
    }
}
