package br.com.apoiosus.controller;

import java.util.Map;

import com.sun.net.httpserver.HttpServer;

import br.com.apoiosus.Http;
import br.com.apoiosus.Http.Resposta;

public class PingController {

    public void registrar(HttpServer servidor) {
        Http.rota(servidor, "GET", "/api/ping", ex -> Resposta.ok(Map.of("status", "ok")));
    }
}
