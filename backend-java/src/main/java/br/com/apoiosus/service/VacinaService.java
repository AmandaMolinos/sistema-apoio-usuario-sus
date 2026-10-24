package br.com.apoiosus.service;

import java.sql.SQLException;
import java.util.List;

import br.com.apoiosus.dto.request.VacinaRequest;
import br.com.apoiosus.dto.response.VacinaResponse;
import br.com.apoiosus.exception.ApiException;
import br.com.apoiosus.model.Vacina;
import br.com.apoiosus.repository.VacinaRepository;

public class VacinaService {

    private final VacinaRepository vacinas;

    public VacinaService(VacinaRepository vacinas) {
        this.vacinas = vacinas;
    }

    public List<VacinaResponse> listar(int usuarioId) throws SQLException {
        return vacinas.listar(usuarioId).stream().map(VacinaResponse::de).toList();
    }

    public VacinaResponse buscar(int usuarioId, int id) throws SQLException {
        Vacina vacina = vacinas.buscar(usuarioId, id);
        if (vacina == null) {
            throw naoEncontrada();
        }
        return VacinaResponse.de(vacina);
    }

    public VacinaResponse criar(int usuarioId, VacinaRequest req) throws SQLException {
        req.validar();
        int id = vacinas.inserir(usuarioId, req.nome().trim(), req.dataAplicacao().trim(),
                req.loteOuNull(), req.localOuNull());
        return buscar(usuarioId, id);
    }

    public VacinaResponse atualizar(int usuarioId, int id, VacinaRequest req) throws SQLException {
        req.validar();
        boolean atualizou = vacinas.atualizar(usuarioId, id, req.nome().trim(), req.dataAplicacao().trim(),
                req.loteOuNull(), req.localOuNull());
        if (!atualizou) {
            throw naoEncontrada();
        }
        return buscar(usuarioId, id);
    }

    public void excluir(int usuarioId, int id) throws SQLException {
        if (!vacinas.excluir(usuarioId, id)) {
            throw naoEncontrada();
        }
    }

    // Vacina de outro usuário também responde "não encontrada", para não revelar que ela existe.
    private ApiException naoEncontrada() {
        return new ApiException(404, "VACINA_NAO_ENCONTRADA", "Vacina não encontrada.");
    }
}
