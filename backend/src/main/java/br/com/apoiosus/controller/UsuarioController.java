package br.com.apoiosus.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.apoiosus.config.UsuarioAutenticado;
import br.com.apoiosus.dto.response.UsuarioResponse;
import br.com.apoiosus.service.UsuarioService;

@RestController
@RequestMapping("/api/v1/usuarios/me")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public UsuarioResponse buscar(@AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return usuarioService.buscar(autenticado.usuarioId());
    }
}
