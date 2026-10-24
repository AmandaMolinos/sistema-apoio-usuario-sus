package br.com.apoiosus.dto.request;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

import br.com.apoiosus.exception.ApiException;

public record VacinaRequest(String nome, String dataAplicacao, String lote, String localAplicacao) {

    public void validar() {
        Map<String, String> erros = new LinkedHashMap<>();

        if (nome == null || nome.isBlank()) {
            erros.put("nome", "Informe o nome da vacina.");
        } else if (nome.length() > 150) {
            erros.put("nome", "O nome da vacina deve ter no máximo 150 caracteres.");
        }

        if (dataAplicacao == null || dataAplicacao.isBlank()) {
            erros.put("dataAplicacao", "Informe a data de aplicação.");
        } else {
            try {
                if (LocalDate.parse(dataAplicacao.trim()).isAfter(LocalDate.now())) {
                    erros.put("dataAplicacao", "A data de aplicação não pode ser futura.");
                }
            } catch (DateTimeParseException e) {
                erros.put("dataAplicacao", "Data inválida. Use o formato aaaa-mm-dd.");
            }
        }

        if (lote != null && lote.length() > 100) {
            erros.put("lote", "O lote deve ter no máximo 100 caracteres.");
        }

        if (localAplicacao != null && localAplicacao.length() > 200) {
            erros.put("localAplicacao", "O local deve ter no máximo 200 caracteres.");
        }

        if (!erros.isEmpty()) {
            throw ApiException.validacao(erros);
        }
    }

    public String loteOuNull() {
        return lote == null || lote.isBlank() ? null : lote.trim();
    }

    public String localOuNull() {
        return localAplicacao == null || localAplicacao.isBlank() ? null : localAplicacao.trim();
    }
}
