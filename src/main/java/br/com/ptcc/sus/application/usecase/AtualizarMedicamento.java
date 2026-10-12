package br.com.ptcc.sus.application.usecase;

import br.com.ptcc.sus.application.port.MedicamentoPort;
import br.com.ptcc.sus.application.port.UsuarioPort;
import br.com.ptcc.sus.domain.model.Medicamento;
import java.time.LocalDate;

public class AtualizarMedicamento {
    private final UsuarioPort usuarios;
    private final MedicamentoPort medicamentos;

    public AtualizarMedicamento(UsuarioPort usuarios, MedicamentoPort medicamentos) {
        this.usuarios = usuarios;
        this.medicamentos = medicamentos;
    }

    public Medicamento executar(String email, Long id, String nome, String dosagem, String frequencia,
                                LocalDate dataInicio, LocalDate dataFim) {
        var usuario = usuarios.buscarPorEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        Medicamento medicamento = medicamentos.buscarPorIdEUsuarioId(id, usuario.id())
                .orElseThrow(() -> new IllegalArgumentException("Medicamento não encontrado."));
        medicamento.atualizar(nome, dosagem, frequencia, dataInicio, dataFim);
        return medicamentos.salvar(medicamento);
    }
}
