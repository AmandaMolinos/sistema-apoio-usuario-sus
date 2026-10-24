package br.com.apoiosus.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import br.com.apoiosus.dto.response.OrientacaoResponse;
import br.com.apoiosus.dto.response.OrientacoesResponse;
import br.com.apoiosus.model.Orientacao;

// Orientações gerais de saúde. O texto fica no arquivo orientacoes.json (em resources), lido uma vez ao subir a API.
// Para mudar ou acrescentar orientações, edite o arquivo: categoria é Prevenção, Doenças ou Hábitos, e
// icone é o nome de uma ilustração do app (gripe, alimentacao, atividade, saude_mental, vacina, sono, higiene, generico).
public class OrientacaoService {

    private final OrientacoesResponse resposta;

    public OrientacaoService() {
        List<Orientacao> orientacoes;
        try (InputStream in = OrientacaoService.class.getClassLoader().getResourceAsStream("orientacoes.json");
                Reader leitor = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            orientacoes = new Gson().fromJson(leitor, new TypeToken<List<Orientacao>>() {
            }.getType());
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler orientacoes.json", e);
        }

        List<OrientacaoResponse> itens = new ArrayList<>();
        for (int i = 0; i < orientacoes.size(); i++) {
            Orientacao o = orientacoes.get(i);
            itens.add(new OrientacaoResponse(i + 1, o.titulo(), o.resumo(), o.categoria(), o.icone()));
        }
        this.resposta = new OrientacoesResponse(itens);
    }

    public OrientacoesResponse listar() {
        return resposta;
    }
}
