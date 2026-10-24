package br.com.ptcc.sus.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "usuario", uniqueConstraints = @UniqueConstraint(name = "uq_usuario_email", columnNames = "email"))
public class UsuarioJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    public Long id;
    @Column(nullable = false, length = 150)
    public String nome;
    @Column(nullable = false, unique = true, length = 255)
    public String email;
    @Column(nullable = false, length = 255)
    public String senha;
    @Column(name = "data_nascimento")
    public LocalDate dataNascimento;

    protected UsuarioJpa() {
    }

    public UsuarioJpa(String n, String e, String s, LocalDate d) {
        nome = n;
        email = e;
        senha = s;
        dataNascimento = d;
    }
}