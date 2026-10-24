package br.com.apoiosus.dto.request;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

import br.com.apoiosus.exception.ApiException;

// Nome, dosagem, frequência e data de início são obrigatórios (mesma regra do backend Spring do projeto);
// só a data final é opcional.
public record MedicamentoRequest(String nome, String dosagem, String frequencia, String dataInicio,
        String dataFim) {

    public void validar() {
        Map<String, String> erros = new LinkedHashMap<>();

        if (nome == null || nome.isBlank()) {
            erros.put("nome", "Informe o nome do medicamento.");
        } else if (nome.length() > 150) {
            erros.put("nome", "O nome do medicamento deve ter no máximo 150 caracteres.");
        }

        if (dosagem == null || dosagem.isBlank()) {
            erros.put("dosagem", "Informe a dosagem.");
        } else if (dosagem.length() > 100) {
            erros.put("dosagem", "A dosagem deve ter no máximo 100 caracteres.");
        }

        if (frequencia == null || frequencia.isBlank()) {
            erros.put("frequencia", "Informe a frequência.");
        } else if (frequencia.length() > 150) {
            erros.put("frequencia", "A frequência deve ter no máximo 150 caracteres.");
        }

        LocalDate inicio = lerData("dataInicio", dataInicio, erros);
        if (dataInicio == null || dataInicio.isBlank()) {
            erros.put("dataInicio", "Informe a data de início.");
        }
        LocalDate fim = lerData("dataFim", dataFim, erros);
        if (inicio != null && fim != null && fim.isBefore(inicio)) {
            erros.put("dataFim", "A data final não pode ser anterior à data de início.");
        }

        if (!erros.isEmpty()) {
            throw ApiException.validacao(erros);
        }
    }

    // Devolve null quando o valor está vazio ou inválido (neste caso registra o erro do campo).
    private static LocalDate lerData(String campo, String valor, Map<String, String> erros) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(valor.trim());
        } catch (DateTimeParseException e) {
            erros.put(campo, "Data inválida. Use o formato aaaa-mm-dd.");
            return null;
        }
    }

    public String dosagemOuNull() {
        return dosagem == null || dosagem.isBlank() ? null : dosagem.trim();
    }

    public String frequenciaOuNull() {
        return frequencia == null || frequencia.isBlank() ? null : frequencia.trim();
    }

    public String dataInicioOuNull() {
        return dataInicio == null || dataInicio.isBlank() ? null : dataInicio.trim();
    }

    public String dataFimOuNull() {
        return dataFim == null || dataFim.isBlank() ? null : dataFim.trim();
    }
}
