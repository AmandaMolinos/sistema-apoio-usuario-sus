package br.com.apoiosus.dto.response;

import java.util.List;

public record SintomaResultadoResponse(String nome, List<String> cuidados, List<String> procurar) {
}
