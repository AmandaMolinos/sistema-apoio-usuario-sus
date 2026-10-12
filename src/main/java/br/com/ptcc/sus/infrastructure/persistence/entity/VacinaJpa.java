package br.com.ptcc.sus.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "vacina", indexes = @Index(name = "idx_vacina_usuario", columnList = "id_usuario"))
public class VacinaJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vacina")
    public Long id;

    @Column(name = "id_usuario", nullable = false)
    public Long usuarioId;

    @Column(name = "nome_vacina", nullable = false, length = 150)
    public String nome;

    @Column(name = "data_aplicacao", nullable = false)
    public LocalDate dataAplicacao;

    @Column(length = 100)
    public String lote;

    @Column(name = "local_aplicacao", length = 200)
    public String localAplicacao;

    protected VacinaJpa() {}

    public VacinaJpa(Long usuarioId, String nome, LocalDate dataAplicacao, String lote, String localAplicacao) {
        this.usuarioId = usuarioId;
        this.nome = nome;
        this.dataAplicacao = dataAplicacao;
        this.lote = lote;
        this.localAplicacao = localAplicacao;
    }
}
