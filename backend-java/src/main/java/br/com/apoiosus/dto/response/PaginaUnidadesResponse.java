package br.com.apoiosus.dto.response;

import java.util.List;

import br.com.apoiosus.model.Unidade;

// proximo = valor de "inicio" para pedir a página seguinte; null quando a fonte não tem mais registros.
public record PaginaUnidadesResponse(List<Unidade> itens, Integer proximo) {
}
