package br.com.apoiosus.dto.response;

import java.util.Map;
import java.util.stream.Collectors;

// Corpo de todo erro da API. Os quatro primeiros campos seguem o ProblemDetail (RFC 7807) usado pelo backend
// Spring do projeto; "codigo", "mensagem" e "erros" (um texto por campo) são nossos e o app depende deles.
public record ErroResponse(String type, String title, int status, String detail, String codigo, String mensagem,
        Map<String, String> erros) {

    public static ErroResponse de(int status, String codigo, String mensagem, Map<String, String> erros) {
        String detail = erros.isEmpty() ? mensagem
                : erros.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue())
                        .collect(Collectors.joining("; "));
        return new ErroResponse("about:blank", titulo(status), status, detail, codigo, mensagem, erros);
    }

    private static String titulo(int status) {
        return switch (status) {
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 409 -> "Conflict";
            case 500 -> "Internal Server Error";
            case 502 -> "Bad Gateway";
            default -> "Error";
        };
    }
}
