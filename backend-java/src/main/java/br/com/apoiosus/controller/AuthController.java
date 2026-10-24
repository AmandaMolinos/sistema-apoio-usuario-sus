package br.com.apoiosus.controller;

import java.util.Map;

import com.sun.net.httpserver.HttpServer;

import br.com.apoiosus.Http;
import br.com.apoiosus.Http.Resposta;
import br.com.apoiosus.dto.request.CadastroRequest;
import br.com.apoiosus.dto.request.LoginRequest;
import br.com.apoiosus.dto.request.RecuperarSenhaRequest;
import br.com.apoiosus.dto.request.RedefinirSenhaRequest;
import br.com.apoiosus.model.Sessao;
import br.com.apoiosus.service.AuthService;
import br.com.apoiosus.service.RecuperacaoSenhaService;

public class AuthController {

    private final AuthService authService;
    private final RecuperacaoSenhaService recuperacaoService;

    public AuthController(AuthService authService, RecuperacaoSenhaService recuperacaoService) {
        this.authService = authService;
        this.recuperacaoService = recuperacaoService;
    }

    public void registrar(HttpServer servidor) {
        Http.rota(servidor, "POST", "/api/auth/cadastro",
                ex -> Resposta.criado(authService.cadastrar(Http.lerCorpo(ex, CadastroRequest.class))));

        Http.rota(servidor, "POST", "/api/auth/login",
                ex -> Resposta.ok(authService.login(Http.lerCorpo(ex, LoginRequest.class))));

        Http.rota(servidor, "POST", "/api/auth/logout", ex -> {
            Sessao sessao = authService.exigirLogin(Http.token(ex));
            authService.logout(sessao.id());
            return Resposta.semConteudo();
        });

        Http.rota(servidor, "POST", "/api/auth/redefinir-senha", ex -> {
            recuperacaoService.redefinir(Http.lerCorpo(ex, RedefinirSenhaRequest.class));
            return Resposta.ok(Map.of("mensagem", "Senha alterada com sucesso. Entre com a nova senha."));
        });

        Http.rota(servidor, "POST", "/api/auth/recuperar-senha", ex -> {
            recuperacaoService.solicitar(Http.lerCorpo(ex, RecuperarSenhaRequest.class));
            return Resposta.ok(Map.of("mensagem",
                    "Se o e-mail estiver cadastrado, enviaremos as instruções de recuperação."));
        });
    }
}
