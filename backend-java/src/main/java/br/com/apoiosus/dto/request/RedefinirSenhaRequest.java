package br.com.apoiosus.dto.request;

import java.util.LinkedHashMap;
import java.util.Map;

import br.com.apoiosus.exception.ApiException;

public record RedefinirSenhaRequest(String codigo, String novaSenha) {

    public void validar() {
        Map<String, String> erros = new LinkedHashMap<>();

        if (codigo == null || codigo.isBlank()) {
            erros.put("codigo", "Informe o código.");
        }

        if (novaSenha == null || novaSenha.isBlank()) {
            erros.put("novaSenha", "Informe a nova senha.");
        } else if (novaSenha.length() < 8 || novaSenha.length() > 72) {
            erros.put("novaSenha", "A senha deve ter entre 8 e 72 caracteres.");
        }

        if (!erros.isEmpty()) {
            throw ApiException.validacao(erros);
        }
    }
}
