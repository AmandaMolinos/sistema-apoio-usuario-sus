package br.com.apoiosus;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import br.com.apoiosus.dto.response.ErroResponse;
import br.com.apoiosus.exception.ApiException;

// Funções de apoio para os controllers: registrar rota, ler o JSON, enviar a resposta, pegar o token.
public class Http {

    // serializeNulls: campos sem valor aparecem como null no JSON.
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    public record Resposta(int status, Object corpo) {
        public static Resposta ok(Object corpo) {
            return new Resposta(200, corpo);
        }

        public static Resposta criado(Object corpo) {
            return new Resposta(201, corpo);
        }

        public static Resposta semConteudo() {
            return new Resposta(204, null);
        }
    }

    public interface Acao {
        Resposta executar(HttpExchange ex) throws Exception;
    }

    // Rota com um único método HTTP.
    public static void rota(HttpServer servidor, String metodo, String caminho, Acao acao) {
        rotas(servidor, caminho, Map.of(metodo, acao));
    }

    // Rota com vários métodos no mesmo caminho (ex.: GET e PUT em /usuarios/me).
    // Confere o método HTTP, executa a ação e converte erros em JSON.
    public static void rotas(HttpServer servidor, String caminho, Map<String, Acao> acoes) {
        servidor.createContext(caminho, ex -> {
            Resposta resposta;
            try {
                Acao acao = acoes.get(ex.getRequestMethod());
                if (acao == null) {
                    throw new ApiException(405, "METODO_NAO_PERMITIDO", "Método não permitido para esta rota.");
                }
                resposta = acao.executar(ex);
            } catch (ApiException e) {
                resposta = new Resposta(e.getStatus(),
                        ErroResponse.de(e.getStatus(), e.getCodigo(), e.getMessage(), e.getErros()));
            } catch (Exception e) {
                e.printStackTrace();
                resposta = new Resposta(500, ErroResponse.de(500, "ERRO_INTERNO",
                        "Erro interno. Tente novamente mais tarde.", Map.of()));
            }
            enviar(ex, resposta);
        });
    }

    // Qualquer caminho que não tenha rota cai aqui.
    public static void rotaNaoEncontrada(HttpServer servidor) {
        servidor.createContext("/", ex -> enviar(ex, new Resposta(404,
                ErroResponse.de(404, "ROTA_NAO_ENCONTRADA", "Rota não encontrada.", Map.of()))));
    }

    // Lê o corpo da requisição como JSON. Ausente ou inválido vira erro 400.
    public static <T> T lerCorpo(HttpExchange ex, Class<T> tipo) throws IOException {
        String json = new String(ex.getRequestBody().readNBytes(100_000), StandardCharsets.UTF_8);
        try {
            T corpo = GSON.fromJson(json, tipo);
            if (corpo == null) {
                throw ApiException.requisicaoInvalida();
            }
            return corpo;
        } catch (JsonSyntaxException e) {
            throw ApiException.requisicaoInvalida();
        }
    }

    // Lê o id do fim do caminho (ex.: /vacinas/12 -> 12). Se não for um número, a rota não existe (404).
    public static int idDoCaminho(HttpExchange ex) {
        String caminho = ex.getRequestURI().getPath();
        String ultimo = caminho.substring(caminho.lastIndexOf('/') + 1);
        try {
            return Integer.parseInt(ultimo);
        } catch (NumberFormatException e) {
            throw new ApiException(404, "ROTA_NAO_ENCONTRADA", "Rota não encontrada.");
        }
    }

    // Valor de um parâmetro da URL (?nome=valor), ou null se não vier.
    public static String parametro(HttpExchange ex, String nome) {
        String consulta = ex.getRequestURI().getRawQuery();
        if (consulta == null) {
            return null;
        }
        for (String par : consulta.split("&")) {
            int igual = par.indexOf('=');
            String chave = URLDecoder.decode(igual < 0 ? par : par.substring(0, igual), StandardCharsets.UTF_8);
            if (chave.equals(nome)) {
                return igual < 0 ? "" : URLDecoder.decode(par.substring(igual + 1), StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    // Token do cabeçalho "Authorization: Bearer <token>", ou null se não vier.
    public static String token(HttpExchange ex) {
        String cabecalho = ex.getRequestHeaders().getFirst("Authorization");
        if (cabecalho == null || !cabecalho.startsWith("Bearer ")) {
            return null;
        }
        return cabecalho.substring("Bearer ".length()).trim();
    }

    private static void enviar(HttpExchange ex, Resposta resposta) throws IOException {
        try (ex) {
            ex.getResponseHeaders().set("Cache-Control", "no-store");
            if (resposta.corpo() == null) {
                ex.sendResponseHeaders(resposta.status(), -1);
                return;
            }
            byte[] bytes = GSON.toJson(resposta.corpo()).getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().set("Content-Type", "application/json;charset=UTF-8");
            ex.sendResponseHeaders(resposta.status(), bytes.length);
            ex.getResponseBody().write(bytes);
        }
    }
}
