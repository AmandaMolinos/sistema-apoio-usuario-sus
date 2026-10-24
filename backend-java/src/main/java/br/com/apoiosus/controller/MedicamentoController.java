package br.com.apoiosus.controller;

import java.util.Map;

import com.sun.net.httpserver.HttpServer;

import br.com.apoiosus.Http;
import br.com.apoiosus.Http.Resposta;
import br.com.apoiosus.dto.request.MedicamentoRequest;
import br.com.apoiosus.model.Sessao;
import br.com.apoiosus.service.AuthService;
import br.com.apoiosus.service.MedicamentoService;

public class MedicamentoController {

    private final AuthService authService;
    private final MedicamentoService medicamentoService;

    public MedicamentoController(AuthService authService, MedicamentoService medicamentoService) {
        this.authService = authService;
        this.medicamentoService = medicamentoService;
    }

    public void registrar(HttpServer servidor) {
        // /usuarios/me/medicamentos  -> lista e cadastro
        Http.rotas(servidor, "/api/medicamentos", Map.of(
                "GET", ex -> {
                    Sessao sessao = authService.exigirLogin(Http.token(ex));
                    return Resposta.ok(medicamentoService.listar(sessao.usuarioId()));
                },
                "POST", ex -> {
                    Sessao sessao = authService.exigirLogin(Http.token(ex));
                    var dados = Http.lerCorpo(ex, MedicamentoRequest.class);
                    return Resposta.criado(medicamentoService.criar(sessao.usuarioId(), dados));
                }));

        // /usuarios/me/medicamentos/{id}  -> consulta, edição e exclusão de um medicamento
        Http.rotas(servidor, "/api/medicamentos/", Map.of(
                "GET", ex -> {
                    Sessao sessao = authService.exigirLogin(Http.token(ex));
                    return Resposta.ok(medicamentoService.buscar(sessao.usuarioId(), Http.idDoCaminho(ex)));
                },
                "PUT", ex -> {
                    Sessao sessao = authService.exigirLogin(Http.token(ex));
                    int id = Http.idDoCaminho(ex);
                    var dados = Http.lerCorpo(ex, MedicamentoRequest.class);
                    return Resposta.ok(medicamentoService.atualizar(sessao.usuarioId(), id, dados));
                },
                "DELETE", ex -> {
                    Sessao sessao = authService.exigirLogin(Http.token(ex));
                    medicamentoService.excluir(sessao.usuarioId(), Http.idDoCaminho(ex));
                    return Resposta.semConteudo();
                }));
    }
}
