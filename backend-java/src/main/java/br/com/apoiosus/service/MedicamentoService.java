package br.com.apoiosus.service;

import java.sql.SQLException;
import java.util.List;

import br.com.apoiosus.dto.request.MedicamentoRequest;
import br.com.apoiosus.dto.response.MedicamentoResponse;
import br.com.apoiosus.exception.ApiException;
import br.com.apoiosus.model.Medicamento;
import br.com.apoiosus.repository.MedicamentoRepository;

public class MedicamentoService {

    private final MedicamentoRepository medicamentos;

    public MedicamentoService(MedicamentoRepository medicamentos) {
        this.medicamentos = medicamentos;
    }

    public List<MedicamentoResponse> listar(int usuarioId) throws SQLException {
        return medicamentos.listar(usuarioId).stream().map(MedicamentoResponse::de).toList();
    }

    public MedicamentoResponse buscar(int usuarioId, int id) throws SQLException {
        Medicamento medicamento = medicamentos.buscar(usuarioId, id);
        if (medicamento == null) {
            throw naoEncontrado();
        }
        return MedicamentoResponse.de(medicamento);
    }

    public MedicamentoResponse criar(int usuarioId, MedicamentoRequest req) throws SQLException {
        req.validar();
        int id = medicamentos.inserir(usuarioId, req.nome().trim(), req.dosagemOuNull(),
                req.frequenciaOuNull(), req.dataInicioOuNull(), req.dataFimOuNull());
        return buscar(usuarioId, id);
    }

    public MedicamentoResponse atualizar(int usuarioId, int id, MedicamentoRequest req) throws SQLException {
        req.validar();
        boolean atualizou = medicamentos.atualizar(usuarioId, id, req.nome().trim(), req.dosagemOuNull(),
                req.frequenciaOuNull(), req.dataInicioOuNull(), req.dataFimOuNull());
        if (!atualizou) {
            throw naoEncontrado();
        }
        return buscar(usuarioId, id);
    }

    public void excluir(int usuarioId, int id) throws SQLException {
        if (!medicamentos.excluir(usuarioId, id)) {
            throw naoEncontrado();
        }
    }

    // Medicamento de outro usuário também responde "não encontrado", para não revelar que ele existe.
    private ApiException naoEncontrado() {
        return new ApiException(404, "MEDICAMENTO_NAO_ENCONTRADO", "Medicamento não encontrado.");
    }
}
