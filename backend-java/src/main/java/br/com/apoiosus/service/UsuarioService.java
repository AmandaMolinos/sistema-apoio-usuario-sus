package br.com.apoiosus.service;

import java.sql.SQLException;

import br.com.apoiosus.dto.request.AtualizarContaRequest;
import br.com.apoiosus.dto.response.UsuarioResponse;
import br.com.apoiosus.exception.ApiException;
import br.com.apoiosus.model.Usuario;
import br.com.apoiosus.repository.UsuarioRepository;

public class UsuarioService {

    private final UsuarioRepository usuarios;

    public UsuarioService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    // Só nome e data de nascimento podem ser alterados (o e-mail identifica a conta).
    public UsuarioResponse atualizar(int usuarioId, AtualizarContaRequest req) throws SQLException {
        req.validar();
        usuarios.atualizar(usuarioId, req.nome().trim(), req.dataNascimentoOuNull());
        return buscar(usuarioId);
    }

    public void excluir(int usuarioId) throws SQLException {
        usuarios.excluir(usuarioId);
    }

    public UsuarioResponse buscar(int usuarioId) throws SQLException {
        Usuario usuario = usuarios.buscarPorId(usuarioId);
        if (usuario == null) {
            throw new ApiException(404, "USUARIO_NAO_ENCONTRADO", "Usuário não encontrado.");
        }
        return UsuarioResponse.de(usuario);
    }
}
