package br.com.apoiosus.model;

public record Medicamento(int id, String nomeMedicamento, String dosagem, String frequencia, String dataInicio,
        String dataFim) {
}
