package br.com.ptcc.sus.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "medicamento", indexes = @Index(name = "idx_medicamento_usuario", columnList = "id_usuario"))
public class MedicamentoJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_medicamento")
    public Long id;

    @Column(name = "id_usuario", nullable = false)
    public Long usuarioId;

    @Column(name = "nome_medicamento", nullable = false, length = 150)
    public String nome;

    @Column(nullable = false, length = 100)
    public String dosagem;

    @Column(nullable = false, length = 150)
    public String frequencia;

    @Column(name = "data_inicio", nullable = false)
    public LocalDate dataInicio;

    @Column(name = "data_fim")
    public LocalDate dataFim;

    protected MedicamentoJpa() {}

    public MedicamentoJpa(Long usuarioId, String nome, String dosagem, String frequencia,
                          LocalDate dataInicio, LocalDate dataFim) {
        this.usuarioId = usuarioId;
        this.nome = nome;
        this.dosagem = dosagem;
        this.frequencia = frequencia;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }
}
