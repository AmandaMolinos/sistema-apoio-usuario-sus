package br.com.apoiosus.dto.request;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

import br.com.apoiosus.exception.ApiException;

public record AtualizarContaRequest(String nome, String dataNascimento) {

    public void validar() {
        Map<String, String> erros = new LinkedHashMap<>();

        if (nome == null || nome.isBlank()) {
            erros.put("nome", "Informe o nome.");
        } else if (nome.length() > 120) {
            erros.put("nome", "O nome deve ter no máximo 120 caracteres.");
        }

        if (dataNascimento != null && !dataNascimento.isBlank()) {
            try {
                if (LocalDate.parse(dataNascimento.trim()).isAfter(LocalDate.now())) {
                    erros.put("dataNascimento", "A data de nascimento não pode ser futura.");
                }
            } catch (DateTimeParseException e) {
                erros.put("dataNascimento", "Data inválida. Use o formato aaaa-mm-dd.");
            }
        }

        if (!erros.isEmpty()) {
            throw ApiException.validacao(erros);
        }
    }

    public String dataNascimentoOuNull() {
        return dataNascimento == null || dataNascimento.isBlank() ? null : dataNascimento.trim();
    }
}
