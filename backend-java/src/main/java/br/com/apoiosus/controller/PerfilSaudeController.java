package br.com.apoiosus.controller;

import java.util.Map;

import com.sun.net.httpserver.HttpServer;

import br.com.apoiosus.Http;
import br.com.apoiosus.Http.Resposta;
import br.com.apoiosus.dto.request.PerfilSaudeRequest;
import br.com.apoiosus.model.Sessao;
import br.com.apoiosus.service.AuthService;
import br.com.apoiosus.service.PerfilSaudeService;

public class PerfilSaudeController {

    private final AuthService authService;
    private final PerfilSaudeService perfilService;

    public PerfilSaudeController(AuthService authService, PerfilSaudeService perfilService) {
        this.authService = authService;
        this.perfilService = perfilService;
    }

    public void registrar(HttpServer servidor) {
        Http.rotas(servidor, "/api/perfil", Map.of(
                "GET", ex -> {
                    Sessao sessao = authService.exigirLogin(Http.token(ex));
                    return Resposta.ok(perfilService.buscar(sessao.usuarioId()));
                },
                "PUT", ex -> {
                    Sessao sessao = authService.exigirLogin(Http.token(ex));
                    var dados = Http.lerCorpo(ex, PerfilSaudeRequest.class);
                    return Resposta.ok(perfilService.atualizar(sessao.usuarioId(), dados));
                },
                "DELETE", ex -> {
                    Sessao sessao = authService.exigirLogin(Http.token(ex));
                    perfilService.limpar(sessao.usuarioId());
                    return Resposta.semConteudo();
                }));
    }
}
