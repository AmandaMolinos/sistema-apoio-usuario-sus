package br.com.ptcc.sus.domain.model;

import java.time.LocalDate;

public class Medicamento {
    private final Long id;
    private final Long usuarioId;
    private String nome;
    private String dosagem;
    private String frequencia;
    private LocalDate dataInicio;
    private LocalDate dataFim;

    public Medicamento(Long id, Long usuarioId, String nome, String dosagem, String frequencia,
                       LocalDate dataInicio, LocalDate dataFim) {
        this.id = id;
        this.usuarioId = usuarioId;
        atualizar(nome, dosagem, frequencia, dataInicio, dataFim);
    }

    public Long id() { return id; }
    public Long usuarioId() { return usuarioId; }
    public String nome() { return nome; }
    public String dosagem() { return dosagem; }
    public String frequencia() { return frequencia; }
    public LocalDate dataInicio() { return dataInicio; }
    public LocalDate dataFim() { return dataFim; }

    public void atualizar(String nome, String dosagem, String frequencia,
                          LocalDate dataInicio, LocalDate dataFim) {
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome do medicamento é obrigatório.");
        if (dosagem == null || dosagem.isBlank()) throw new IllegalArgumentException("Dosagem é obrigatória.");
        if (frequencia == null || frequencia.isBlank()) throw new IllegalArgumentException("Frequência de uso é obrigatória.");
        if (dataInicio == null) throw new IllegalArgumentException("Data de início é obrigatória.");
        if (dataFim != null && dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("A data final não pode ser anterior à data inicial.");
        }
        this.nome = nome.trim();
        this.dosagem = dosagem.trim();
        this.frequencia = frequencia.trim();
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }
}
