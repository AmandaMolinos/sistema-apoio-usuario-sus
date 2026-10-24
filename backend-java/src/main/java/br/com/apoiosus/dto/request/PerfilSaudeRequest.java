package br.com.apoiosus.dto.request;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import br.com.apoiosus.exception.ApiException;

// Peso em kg e altura em metros. Todos os campos são opcionais.
public record PerfilSaudeRequest(Double peso, Double altura, String tipoSanguineo, String alergias,
        String observacoesMedicas) {

    // Mesmo formato aceito pelo backend Spring do projeto: A, B, AB ou O, com + ou - opcional.
    private static final Pattern TIPO = Pattern.compile("^(A|B|AB|O)[+-]?$");

    public void validar() {
        Map<String, String> erros = new LinkedHashMap<>();

        if (peso != null && (peso <= 0 || peso > 1000)) {
            erros.put("peso", "O peso deve ser maior que zero e no máximo 1000 kg.");
        }

        if (altura != null && (altura <= 0 || altura > 3)) {
            erros.put("altura", "A altura deve ser maior que zero e no máximo 3 m.");
        }

        if (tipoSanguineoOuNull() != null && !TIPO.matcher(tipoSanguineoOuNull()).matches()) {
            erros.put("tipoSanguineo", "Tipo sanguíneo inválido. Use A, B, AB ou O, com + ou - (ex.: O+).");
        }

        if (alergias != null && alergias.length() > 5000) {
            erros.put("alergias", "As alergias devem ter no máximo 5000 caracteres.");
        }

        if (observacoesMedicas != null && observacoesMedicas.length() > 5000) {
            erros.put("observacoesMedicas", "As observações devem ter no máximo 5000 caracteres.");
        }

        if (!erros.isEmpty()) {
            throw ApiException.validacao(erros);
        }
    }

    // Tipo sanguíneo em maiúsculas, ou null quando vazio.
    public String tipoSanguineoOuNull() {
        return textoOuNull(tipoSanguineo) == null ? null : tipoSanguineo.trim().toUpperCase();
    }

    public String alergiasOuNull() {
        return textoOuNull(alergias);
    }

    public String observacoesOuNull() {
        return textoOuNull(observacoesMedicas);
    }

    private static String textoOuNull(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
