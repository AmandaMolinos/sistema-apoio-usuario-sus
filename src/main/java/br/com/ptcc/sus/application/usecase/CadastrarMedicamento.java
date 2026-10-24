package br.com.ptcc.sus.application.usecase;

import br.com.ptcc.sus.application.port.MedicamentoPort;
import br.com.ptcc.sus.application.port.UsuarioPort;
import br.com.ptcc.sus.domain.model.Medicamento;
import br.com.ptcc.sus.domain.model.Usuario;
import java.time.LocalDate;

public class CadastrarMedicamento {
    private final UsuarioPort usuarios;
    private final MedicamentoPort medicamentos;

    public CadastrarMedicamento(UsuarioPort usuarios, MedicamentoPort medicamentos) {
        this.usuarios = usuarios;
        this.medicamentos = medicamentos;
    }

    public Medicamento executar(String email, String nome, String dosagem, String frequencia,
                                LocalDate dataInicio, LocalDate dataFim) {
        Usuario usuario = usuarios.buscarPorEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        return medicamentos.salvar(new Medicamento(null, usuario.id(), nome, dosagem, frequencia, dataInicio, dataFim));
    }
}
