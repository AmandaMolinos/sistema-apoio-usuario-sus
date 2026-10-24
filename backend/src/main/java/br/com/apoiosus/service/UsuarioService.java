package br.com.apoiosus.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import br.com.apoiosus.dto.response.UsuarioResponse;
import br.com.apoiosus.exception.ApiException;
import br.com.apoiosus.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;

    public UsuarioService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public UsuarioResponse buscar(Integer usuarioId) {
        return usuarios.findById(usuarioId)
                .map(UsuarioResponse::de)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USUARIO_NAO_ENCONTRADO",
                        "Usuário não encontrado."));
    }
}
