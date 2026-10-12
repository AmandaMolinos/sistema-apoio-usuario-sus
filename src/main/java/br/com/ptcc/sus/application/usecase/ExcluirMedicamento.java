package br.com.ptcc.sus.application.usecase;

import br.com.ptcc.sus.application.port.MedicamentoPort;
import br.com.ptcc.sus.application.port.UsuarioPort;

public class ExcluirMedicamento {
    private final UsuarioPort usuarios;
    private final MedicamentoPort medicamentos;

    public ExcluirMedicamento(UsuarioPort usuarios, MedicamentoPort medicamentos) {
        this.usuarios = usuarios;
        this.medicamentos = medicamentos;
    }

    public void executar(String email, Long id) {
        var usuario = usuarios.buscarPorEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        medicamentos.buscarPorIdEUsuarioId(id, usuario.id())
                .orElseThrow(() -> new IllegalArgumentException("Medicamento não encontrado."));
        medicamentos.excluir(id);
    }
}
