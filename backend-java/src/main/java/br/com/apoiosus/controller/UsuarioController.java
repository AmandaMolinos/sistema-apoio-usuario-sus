package br.com.apoiosus.controller;

import java.util.Map;

import com.sun.net.httpserver.HttpServer;

import br.com.apoiosus.Http;
import br.com.apoiosus.Http.Resposta;
import br.com.apoiosus.dto.request.AtualizarContaRequest;
import br.com.apoiosus.model.Sessao;
import br.com.apoiosus.service.AuthService;
import br.com.apoiosus.service.UsuarioService;

public class UsuarioController {

    private final AuthService authService;
    private final UsuarioService usuarioService;

    public UsuarioController(AuthService authService, UsuarioService usuarioService) {
        this.authService = authService;
        this.usuarioService = usuarioService;
    }

    public void registrar(HttpServer servidor) {
        Http.rotas(servidor, "/api/usuarios/me", Map.of(
                "GET", ex -> {
                    Sessao sessao = authService.exigirLogin(Http.token(ex));
                    return Resposta.ok(usuarioService.buscar(sessao.usuarioId()));
                },
                "PUT", ex -> {
                    Sessao sessao = authService.exigirLogin(Http.token(ex));
                    var dados = Http.lerCorpo(ex, AtualizarContaRequest.class);
                    return Resposta.ok(usuarioService.atualizar(sessao.usuarioId(), dados));
                },
                "DELETE", ex -> {
                    Sessao sessao = authService.exigirLogin(Http.token(ex));
                    usuarioService.excluir(sessao.usuarioId());
                    return Resposta.semConteudo();
                }));
    }
}
