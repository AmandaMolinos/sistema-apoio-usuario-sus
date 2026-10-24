package br.com.apoiosus.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.google.gson.Gson;

import br.com.apoiosus.dto.response.AnaliseSintomasResponse;
import br.com.apoiosus.dto.response.SintomaResultadoResponse;

// Análise de sintomas por regras fixas (sem IA e sem diagnóstico): procura no texto as palavras-chave do
// catálogo sintomas.json e devolve orientações gerais escritas com antecedência. Sinais de alerta (falta de ar,
// dor no peito etc.) têm prioridade. O texto digitado não é guardado nem registrado em log.
public class SintomaService {

    static final String AVISO = "Esta funcionalidade oferece apenas orientações informativas e não substitui "
            + "a avaliação de um profissional de saúde.";
    static final String ORIENTACAO_URGENTE = "Procure atendimento agora: ligue 192 (SAMU) ou vá à UPA ou ao "
            + "hospital mais próximo.";

    // Formato do arquivo sintomas.json.
    private record Catalogo(List<Alerta> alertas, List<Sintoma> sintomas) {
    }

    private record Alerta(List<String> palavras, String mensagem) {
    }

    private record Sintoma(String nome, List<String> palavras, List<String> cuidados, List<String> procurar) {
    }

    // Palavras que, até duas palavras antes de um sintoma, indicam que ele foi negado ("sem febre").
    private static final Set<String> NEGACOES = Set.of("sem", "nao", "nem");

    private final Catalogo catalogo;

    public SintomaService() {
        try (InputStream in = SintomaService.class.getClassLoader().getResourceAsStream("sintomas.json");
                Reader leitor = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            catalogo = new Gson().fromJson(leitor, Catalogo.class);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler sintomas.json", e);
        }
    }

    public AnaliseSintomasResponse analisar(String texto) {
        List<String> palavras = Arrays.asList(normalizar(texto).split(" "));

        // Alerta vale mesmo se vier negado: na dúvida, é melhor avisar do que deixar passar.
        List<String> alertas = new ArrayList<>();
        for (Alerta alerta : catalogo.alertas()) {
            if (cita(palavras, alerta.palavras(), false)) {
                alertas.add(alerta.mensagem());
            }
        }

        List<SintomaResultadoResponse> sintomas = new ArrayList<>();
        for (Sintoma sintoma : catalogo.sintomas()) {
            if (cita(palavras, sintoma.palavras(), true)) {
                sintomas.add(new SintomaResultadoResponse(sintoma.nome(), sintoma.cuidados(), sintoma.procurar()));
            }
        }

        boolean urgente = !alertas.isEmpty();
        if (urgente) {
            alertas.add(ORIENTACAO_URGENTE);
        }
        return new AnaliseSintomasResponse(urgente, alertas, sintomas, urgente || !sintomas.isEmpty(), AVISO);
    }

    // O texto cita alguma das expressões? Compara palavras inteiras (sem acento e sem maiúsculas).
    private boolean cita(List<String> palavras, List<String> expressoes, boolean respeitarNegacao) {
        for (String expressao : expressoes) {
            List<String> alvo = Arrays.asList(normalizar(expressao).split(" "));
            for (int i = 0; i + alvo.size() <= palavras.size(); i++) {
                if (palavras.subList(i, i + alvo.size()).equals(alvo)
                        && !(respeitarNegacao && negado(palavras, i))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean negado(List<String> palavras, int inicio) {
        for (int j = Math.max(0, inicio - 2); j < inicio; j++) {
            if (NEGACOES.contains(palavras.get(j))) {
                return true;
            }
        }
        return false;
    }

    // Minúsculas, sem acento e só com letras, números e espaços simples.
    static String normalizar(String texto) {
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").strip();
    }
}
