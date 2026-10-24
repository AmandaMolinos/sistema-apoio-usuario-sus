package br.com.apoiosus.dto.request;

import java.util.Map;

import br.com.apoiosus.exception.ApiException;

public record RecuperarSenhaRequest(String email) {

    public void validar() {
        if (email == null || email.isBlank()) {
            throw ApiException.validacao(Map.of("email", "Informe o e-mail."));
        }
        if (!email.trim().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw ApiException.validacao(Map.of("email", "E-mail inválido."));
        }
    }
}
