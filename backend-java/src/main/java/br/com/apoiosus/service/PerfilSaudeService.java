package br.com.apoiosus.service;

import java.sql.SQLException;

import br.com.apoiosus.dto.request.PerfilSaudeRequest;
import br.com.apoiosus.dto.response.PerfilSaudeResponse;
import br.com.apoiosus.exception.ApiException;
import br.com.apoiosus.repository.PerfilSaudeRepository;

public class PerfilSaudeService {

    private final PerfilSaudeRepository perfis;

    public PerfilSaudeService(PerfilSaudeRepository perfis) {
        this.perfis = perfis;
    }

    public PerfilSaudeResponse buscar(int usuarioId) throws SQLException {
        PerfilSaudeResponse perfil = perfis.buscar(usuarioId);
        if (perfil == null) {
            throw new ApiException(404, "PERFIL_NAO_ENCONTRADO", "Perfil de saúde não encontrado.");
        }
        return perfil;
    }

    // Substitui todos os campos do perfil: o que não for enviado fica vazio.
    public PerfilSaudeResponse atualizar(int usuarioId, PerfilSaudeRequest req) throws SQLException {
        req.validar();
        buscar(usuarioId);
        perfis.atualizar(usuarioId, req.peso(), req.altura(), req.tipoSanguineoOuNull(), req.alergiasOuNull(),
                req.observacoesOuNull());
        return buscar(usuarioId);
    }

    public void limpar(int usuarioId) throws SQLException {
        buscar(usuarioId);
        perfis.limpar(usuarioId);
    }
}
