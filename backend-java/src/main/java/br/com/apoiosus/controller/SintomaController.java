package br.com.apoiosus.controller;

import com.sun.net.httpserver.HttpServer;

import br.com.apoiosus.Http;
import br.com.apoiosus.Http.Resposta;
import br.com.apoiosus.dto.request.SintomaRequest;
import br.com.apoiosus.service.AuthService;
import br.com.apoiosus.service.SintomaService;

public class SintomaController {

    private final AuthService authService;
    private final SintomaService sintomaService;

    public SintomaController(AuthService authService, SintomaService sintomaService) {
        this.authService = authService;
        this.sintomaService = sintomaService;
    }

    public void registrar(HttpServer servidor) {
        // /sintomas/analise  -> orientações informativas a partir do texto dos sintomas (nada é guardado)
        Http.rota(servidor, "POST", "/api/sintomas/analise", ex -> {
            authService.exigirLogin(Http.token(ex));
            var dados = Http.lerCorpo(ex, SintomaRequest.class);
            dados.validar();
            return Resposta.ok(sintomaService.analisar(dados.texto()));
        });
    }
}
