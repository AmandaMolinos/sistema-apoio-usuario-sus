package br.com.apoiosus.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.apoiosus.dto.response.ErroResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ErroResponse> tratarApi(ApiException e) {
        return ResponseEntity.status(e.getStatus()).body(ErroResponse.of(e.getCodigo(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErroResponse> tratarValidacao(MethodArgumentNotValidException e) {
        Map<String, String> erros = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(f -> erros.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(new ErroResponse("VALIDACAO", "Dados inválidos. Verifique os campos informados.", erros));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErroResponse> tratarCorpoIlegivel(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest()
                .body(ErroResponse.of("REQUISICAO_INVALIDA", "Corpo da requisição ausente ou malformado."));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErroResponse> tratarInesperado(Exception e) {
        log.error("Erro inesperado", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErroResponse.of("ERRO_INTERNO", "Erro interno. Tente novamente mais tarde."));
    }
}
