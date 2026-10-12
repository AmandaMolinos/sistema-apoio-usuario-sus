package br.com.ptcc.sus.interfaces.rest;

import br.com.ptcc.sus.application.usecase.*;
import br.com.ptcc.sus.application.port.TokenPort;
import br.com.ptcc.sus.interfaces.rest.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final CadastroUsuario cadastro;
    private final LoginUsuario login;
    private final TokenPort tokens;

    public AuthController(CadastroUsuario c, LoginUsuario l, TokenPort t) {
        cadastro = c;
        login = l;
        tokens = t;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<Responses.Usuario> cadastro(@Valid @RequestBody Requests.Cadastro r) {
        var u = cadastro.executar(r.nome(), r.email(), r.senha(), r.dataNascimento());
        return ResponseEntity.status(HttpStatus.CREATED).body(Responses.Usuario.of(u));
    }

    @PostMapping("/login")
    public Responses.Auth login(@Valid @RequestBody Requests.Login r) {
        return new Responses.Auth(login.executar(r.email(), r.senha()), "Bearer", tokens.expiracaoSegundos());
    }
}