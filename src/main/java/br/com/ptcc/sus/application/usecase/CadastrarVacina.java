package br.com.ptcc.sus.application.usecase;

import br.com.ptcc.sus.application.port.UsuarioPort;
import br.com.ptcc.sus.application.port.VacinaPort;
import br.com.ptcc.sus.domain.model.Usuario;
import br.com.ptcc.sus.domain.model.Vacina;
import java.time.LocalDate;

public class CadastrarVacina {
    private final UsuarioPort usuarios;
    private final VacinaPort vacinas;

    public CadastrarVacina(UsuarioPort usuarios, VacinaPort vacinas) {
        this.usuarios = usuarios;
        this.vacinas = vacinas;
    }

    public Vacina executar(String email, String nome, LocalDate dataAplicacao, String lote, String localAplicacao) {
        Usuario usuario = usuarios.buscarPorEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        return vacinas.salvar(new Vacina(null, usuario.id(), nome, dataAplicacao, lote, localAplicacao));
    }
}
