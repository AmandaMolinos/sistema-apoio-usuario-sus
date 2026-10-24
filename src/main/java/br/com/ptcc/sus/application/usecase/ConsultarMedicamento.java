package br.com.ptcc.sus.application.usecase;

import br.com.ptcc.sus.application.port.MedicamentoPort;
import br.com.ptcc.sus.application.port.UsuarioPort;
import br.com.ptcc.sus.domain.model.Medicamento;

public class ConsultarMedicamento {
    private final UsuarioPort usuarios;
    private final MedicamentoPort medicamentos;

    public ConsultarMedicamento(UsuarioPort usuarios, MedicamentoPort medicamentos) {
        this.usuarios = usuarios;
        this.medicamentos = medicamentos;
    }

    public Medicamento executar(String email, Long id) {
        var usuario = usuarios.buscarPorEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        return medicamentos.buscarPorIdEUsuarioId(id, usuario.id())
                .orElseThrow(() -> new IllegalArgumentException("Medicamento não encontrado."));
    }
}
