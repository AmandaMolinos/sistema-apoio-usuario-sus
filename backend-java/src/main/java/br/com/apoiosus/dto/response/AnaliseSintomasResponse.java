package br.com.apoiosus.dto.response;

import java.util.List;

// urgente: algum sinal de alerta foi citado (alertas traz as mensagens, com a orientação de ligar 192 no fim).
// reconhecido: o texto citou algum sintoma ou sinal de alerta conhecido.
// aviso: texto fixo de que isto não substitui um profissional de saúde.
public record AnaliseSintomasResponse(boolean urgente, List<String> alertas, List<SintomaResultadoResponse> sintomas,
        boolean reconhecido, String aviso) {
}
