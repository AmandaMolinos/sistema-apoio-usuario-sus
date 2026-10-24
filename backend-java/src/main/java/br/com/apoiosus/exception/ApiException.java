package br.com.apoiosus.exception;

import java.util.Map;

// Erro "esperado" da API: vira uma resposta JSON com o status, o código e a mensagem.
public class ApiException extends RuntimeException {

    private final int status;
    private final String codigo;
    private final Map<String, String> erros;

    public ApiException(int status, String codigo, String mensagem) {
        this(status, codigo, mensagem, Map.of());
    }

    public ApiException(int status, String codigo, String mensagem, Map<String, String> erros) {
        super(mensagem);
        this.status = status;
        this.codigo = codigo;
        this.erros = erros;
    }

    public static ApiException validacao(Map<String, String> erros) {
        return new ApiException(400, "VALIDACAO", "Dados inválidos. Verifique os campos informados.", erros);
    }

    public static ApiException requisicaoInvalida() {
        return new ApiException(400, "REQUISICAO_INVALIDA", "Corpo da requisição ausente ou malformado.");
    }

    public static ApiException naoAutenticado() {
        return new ApiException(401, "NAO_AUTENTICADO", "Sessão ausente, inválida ou expirada.");
    }

    public int getStatus() {
        return status;
    }

    public String getCodigo() {
        return codigo;
    }

    public Map<String, String> getErros() {
        return erros;
    }
}
