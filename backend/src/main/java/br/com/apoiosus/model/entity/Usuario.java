package br.com.apoiosus.model.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    // A coluna "senha" guarda o hash, nunca a senha em texto.
    @Column(name = "senha", nullable = false)
    private String senhaHash;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    protected Usuario() {
    }

    public Usuario(String nome, String email, String senhaHash, LocalDate dataNascimento) {
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.dataNascimento = dataNascimento;
    }

    public Integer getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }
}

