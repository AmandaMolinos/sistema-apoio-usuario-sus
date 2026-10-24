package br.com.apoiosus.dto.request;

import java.util.LinkedHashMap;
import java.util.Map;

import br.com.apoiosus.exception.ApiException;

public record LoginRequest(String email, String senha) {

    public void validar() {
        Map<String, String> erros = new LinkedHashMap<>();
        if (email == null || email.isBlank()) {
            erros.put("email", "Informe o e-mail.");
        }
        if (senha == null || senha.isBlank()) {
            erros.put("senha", "Informe a senha.");
        }
        if (!erros.isEmpty()) {
            throw ApiException.validacao(erros);
        }
    }
}
