package br.com.ptcc.sus.application.usecase;

import br.com.ptcc.sus.application.port.*;
import br.com.ptcc.sus.domain.model.*;

import java.time.LocalDate;

public class CadastroUsuario {
    private final UsuarioPort usuarios;
    private final PerfilPort perfis;
    private final SenhaPort senhas;

    public CadastroUsuario(UsuarioPort u, PerfilPort p, SenhaPort s) {
        usuarios = u;
        perfis = p;
        senhas = s;
    }

    public Usuario executar(String nome, String email, String senha, LocalDate nascimento) {
        String normalized = email.trim().toLowerCase();
        if (usuarios.emailExiste(normalized)) throw new IllegalArgumentException("E-mail já cadastrado.");
        Usuario saved = usuarios.salvar(new Usuario(null, nome.trim(), normalized, senhas.hash(senha), nascimento));
        perfis.salvar(new PerfilSaude(null, saved.id(), null, null, null, null, null));
        return saved;
    }
}