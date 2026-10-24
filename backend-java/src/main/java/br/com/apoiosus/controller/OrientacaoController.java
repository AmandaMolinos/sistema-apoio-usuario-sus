package br.com.apoiosus.controller;

import com.sun.net.httpserver.HttpServer;

import br.com.apoiosus.Http;
import br.com.apoiosus.Http.Resposta;
import br.com.apoiosus.service.AuthService;
import br.com.apoiosus.service.OrientacaoService;

public class OrientacaoController {

    private final AuthService authService;
    private final OrientacaoService orientacaoService;

    public OrientacaoController(AuthService authService, OrientacaoService orientacaoService) {
        this.authService = authService;
        this.orientacaoService = orientacaoService;
    }

    public void registrar(HttpServer servidor) {
        // /orientacoes  -> orientações de saúde geradas por IA (com cache de 24 h)
        Http.rota(servidor, "GET", "/api/orientacoes", ex -> {
            authService.exigirLogin(Http.token(ex));
            return Resposta.ok(orientacaoService.listar());
        });
    }
}
