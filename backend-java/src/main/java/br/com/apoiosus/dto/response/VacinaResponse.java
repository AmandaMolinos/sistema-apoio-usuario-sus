package br.com.apoiosus.dto.response;

import br.com.apoiosus.model.Vacina;

public record VacinaResponse(int id, String nome, String dataAplicacao, String lote, String localAplicacao) {

    public static VacinaResponse de(Vacina v) {
        return new VacinaResponse(v.id(), v.nomeVacina(), v.dataAplicacao(), v.lote(), v.localAplicacao());
    }
}
