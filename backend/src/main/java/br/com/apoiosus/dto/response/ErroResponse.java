package br.com.apoiosus.dto.response;

import java.util.Map;

public record ErroResponse(String codigo, String mensagem, Map<String, String> erros) {

    public static ErroResponse of(String codigo, String mensagem) {
        return new ErroResponse(codigo, mensagem, Map.of());
    }
}
