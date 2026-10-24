package br.com.apoiosus.dto.request;

import java.util.Map;

import br.com.apoiosus.exception.ApiException;

public record SintomaRequest(String texto) {

    public static final int MAXIMO = 500;

    public void validar() {
        if (texto == null || texto.isBlank()) {
            throw ApiException.validacao(Map.of("texto", "Descreva os sintomas que você está sentindo."));
        }
        if (texto.length() > MAXIMO) {
            throw ApiException.validacao(Map.of("texto", "A descrição deve ter no máximo " + MAXIMO + " caracteres."));
        }
    }
}
