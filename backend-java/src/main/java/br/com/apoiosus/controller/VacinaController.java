package br.com.apoiosus.controller;

import java.util.Map;

import com.sun.net.httpserver.HttpServer;

import br.com.apoiosus.Http;
import br.com.apoiosus.Http.Resposta;
import br.com.apoiosus.dto.request.VacinaRequest;
import br.com.apoiosus.model.Sessao;
import br.com.apoiosus.service.AuthService;
import br.com.apoiosus.service.VacinaService;

public class VacinaController {

    private final AuthService authService;
    private final VacinaService vacinaService;

    public VacinaController(AuthService authService, VacinaService vacinaService) {
        this.authService = authService;
        this.vacinaService = vacinaService;
    }

    public void registrar(HttpServer servidor) {
        // /usuarios/me/vacinas  -> lista e cadastro
        Http.rotas(servidor, "/api/vacinas", Map.of(
                "GET", ex -> {
                    Sessao sessao = authService.exigirLogin(Http.token(ex));
                    return Resposta.ok(vacinaService.listar(sessao.usuarioId()));
                },
                "POST", ex -> {
                    Sessao sessao = authService.exigirLogin(Http.token(ex));
                    var dados = Http.lerCorpo(ex, VacinaRequest.class);
                    return Resposta.criado(vacinaService.criar(sessao.usuarioId(), dados));
                }));

        // /usuarios/me/vacinas/{id}  -> consulta, edição e exclusão de uma vacina
        Http.rotas(servidor, "/api/vacinas/", Map.of(
                "GET", ex -> {
                    Sessao sessao = authService.exigirLogin(Http.token(ex));
                    return Resposta.ok(vacinaService.buscar(sessao.usuarioId(), Http.idDoCaminho(ex)));
                },
                "PUT", ex -> {
                    Sessao sessao = authService.exigirLogin(Http.token(ex));
                    int id = Http.idDoCaminho(ex);
                    var dados = Http.lerCorpo(ex, VacinaRequest.class);
                    return Resposta.ok(vacinaService.atualizar(sessao.usuarioId(), id, dados));
                },
                "DELETE", ex -> {
                    Sessao sessao = authService.exigirLogin(Http.token(ex));
                    vacinaService.excluir(sessao.usuarioId(), Http.idDoCaminho(ex));
                    return Resposta.semConteudo();
                }));
    }
}
