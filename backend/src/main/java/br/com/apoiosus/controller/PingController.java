package br.com.apoiosus.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ping")
public class PingController {

    @GetMapping
    public Map<String, String> ping() {
        return Map.of("status", "ok");
    }
}
