package br.com.ptcc.sus.application.usecase;

import br.com.ptcc.sus.application.port.MedicamentoPort;
import br.com.ptcc.sus.application.port.UsuarioPort;
import br.com.ptcc.sus.domain.model.Medicamento;
import java.util.List;

public class ListarMedicamentos {
    private final UsuarioPort usuarios;
    private final MedicamentoPort medicamentos;

    public ListarMedicamentos(UsuarioPort usuarios, MedicamentoPort medicamentos) {
        this.usuarios = usuarios;
        this.medicamentos = medicamentos;
    }

    public List<Medicamento> executar(String email) {
        var usuario = usuarios.buscarPorEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        return medicamentos.listarPorUsuarioId(usuario.id());
    }
}
