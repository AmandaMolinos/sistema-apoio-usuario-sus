package br.com.apoiosus.controller;

import com.sun.net.httpserver.HttpServer;

import br.com.apoiosus.Http;
import br.com.apoiosus.Http.Resposta;
import br.com.apoiosus.service.AuthService;
import br.com.apoiosus.service.MunicipioService;
import br.com.apoiosus.service.UnidadeSaudeService;

public class UnidadeSaudeController {

    private final AuthService authService;
    private final MunicipioService municipioService;
    private final UnidadeSaudeService unidadeService;

    public UnidadeSaudeController(AuthService authService, MunicipioService municipioService,
            UnidadeSaudeService unidadeService) {
        this.authService = authService;
        this.municipioService = municipioService;
        this.unidadeService = unidadeService;
    }

    public void registrar(HttpServer servidor) {
        // /unidades-saude/municipios  -> municípios do Estado de São Paulo
        Http.rota(servidor, "GET", "/api/unidades-saude/municipios", ex -> {
            authService.exigirLogin(Http.token(ex));
            return Resposta.ok(municipioService.listar());
        });

        // /unidades-saude/upas?municipio=355030&inicio=1  -> UPAs do SUS no município
        Http.rota(servidor, "GET", "/api/unidades-saude/upas", ex -> {
            authService.exigirLogin(Http.token(ex));
            return Resposta.ok(unidadeService.upas(Http.parametro(ex, "municipio"), Http.parametro(ex, "inicio")));
        });
    }
}
