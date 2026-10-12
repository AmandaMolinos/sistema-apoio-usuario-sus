package br.com.ptcc.sus.interfaces.rest;

import br.com.ptcc.sus.application.usecase.*;
import br.com.ptcc.sus.interfaces.rest.dto.*;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {
    private final ConsultarPerfil consultar;
    private final AtualizarPerfil atualizar;

    public PerfilController(ConsultarPerfil c, AtualizarPerfil a) {
        consultar = c;
        atualizar = a;
    }

    @GetMapping
    public Responses.Perfil consultar(Authentication a) {
        return Responses.Perfil.of(consultar.executar(a.getName()));
    }

    @PutMapping
    public Responses.Perfil atualizar(Authentication a, @Valid @RequestBody Requests.Perfil r) {
        return Responses.Perfil.of(atualizar.executar(a.getName(), r.peso(), r.altura(), r.tipoSanguineo(), r.alergias(), r.observacoesMedicas()));
    }
}