package br.com.apoiosus.dto.response;

import br.com.apoiosus.model.Medicamento;

public record MedicamentoResponse(int id, String nome, String dosagem, String frequencia, String dataInicio,
        String dataFim) {

    public static MedicamentoResponse de(Medicamento m) {
        return new MedicamentoResponse(m.id(), m.nomeMedicamento(), m.dosagem(), m.frequencia(), m.dataInicio(),
                m.dataFim());
    }
}
