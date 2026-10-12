package br.com.ptcc.sus.interfaces.rest;

import br.com.ptcc.sus.application.usecase.ConsultarUsuario;
import br.com.ptcc.sus.interfaces.rest.dto.Responses;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final ConsultarUsuario consultar;

    public UsuarioController(ConsultarUsuario c) {
        consultar = c;
    }

    @GetMapping("/me")
    public Responses.Usuario me(Authentication a) {
        return Responses.Usuario.of(consultar.executar(a.getName()));
    }
}