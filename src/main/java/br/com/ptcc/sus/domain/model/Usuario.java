package br.com.ptcc.sus.domain.model;

import java.time.LocalDate;

public class Usuario {
    private final Long id;
    private String nome;
    private final String email;
    private final String senhaHash;
    private LocalDate dataNascimento;

    public Usuario(Long id, String nome, String email, String senhaHash, LocalDate dataNascimento) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.dataNascimento = dataNascimento;
    }

    public Long id() {
        return id;
    }

    public String nome() {
        return nome;
    }

    public String email() {
        return email;
    }

    public String senhaHash() {
        return senhaHash;
    }

    public LocalDate dataNascimento() {
        return dataNascimento;
    }

    public void atualizarDados(String nome, LocalDate nascimento) {
        this.nome = nome;
        this.dataNascimento = nascimento;
    }
}