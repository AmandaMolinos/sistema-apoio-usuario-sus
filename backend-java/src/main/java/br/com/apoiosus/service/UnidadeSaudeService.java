package br.com.apoiosus.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.apoiosus.dto.response.PaginaUnidadesResponse;
import br.com.apoiosus.exception.ApiException;
import br.com.apoiosus.integration.CnesClient;
import br.com.apoiosus.model.Unidade;

public class UnidadeSaudeService {

    // Quantas páginas do CNES uma única chamada nossa pode ler (limita a demora).
    private static final int MAX_PAGINAS_CNES = 5;

    // A página do CNES só é aproveitada inteira; paramos de ler quando já temos pelo menos isto.
    private static final int MINIMO_DE_ITENS = 20;

    private final MunicipioService municipios;
    private final CnesClient cnes;

    public UnidadeSaudeService(MunicipioService municipios, CnesClient cnes) {
        this.municipios = municipios;
        this.cnes = cnes;
    }

    // UPAs do SUS do município. O CNES não deixa filtrar por SUS, então lemos páginas e descartamos as demais.
    // "inicio" é o registro do CNES por onde começar (1 na primeira chamada; depois, o "proximo" devolvido).
    public PaginaUnidadesResponse upas(String codigoMunicipio, String inicioTexto) {
        Map<String, String> erros = new LinkedHashMap<>();
        if (codigoMunicipio == null || municipios.buscar(codigoMunicipio) == null) {
            erros.put("municipio", "Município inválido. Escolha um município do Estado de São Paulo.");
        }
        int inicio = 1;
        if (inicioTexto != null && !inicioTexto.isBlank()) {
            try {
                inicio = Integer.parseInt(inicioTexto.trim());
            } catch (NumberFormatException e) {
                inicio = 0;
            }
            if (inicio < 1) {
                erros.put("inicio", "Informe um número inteiro maior que zero.");
            }
        }
        if (!erros.isEmpty()) {
            throw ApiException.validacao(erros);
        }

        List<Unidade> itens = new ArrayList<>();
        Integer proximo = inicio;
        for (int leituras = 0; leituras < MAX_PAGINAS_CNES && proximo != null && itens.size() < MINIMO_DE_ITENS;
                leituras++) {
            List<Unidade> pagina = cnes.pesquisarProntoAtendimento(codigoMunicipio, proximo);
            for (Unidade unidade : pagina) {
                if (unidade.atendeSus()) {
                    itens.add(unidade);
                }
            }
            // Página incompleta significa que o CNES não tem mais registros.
            proximo = pagina.size() < CnesClient.POR_PAGINA ? null : proximo + pagina.size();
        }
        return new PaginaUnidadesResponse(itens, proximo);
    }
}
