package br.com.ptcc.sus.domain.model;

import java.time.LocalDate;

public class Vacina {
    private final Long id;
    private final Long usuarioId;
    private String nome;
    private LocalDate dataAplicacao;
    private String lote;
    private String localAplicacao;

    public Vacina(Long id, Long usuarioId, String nome, LocalDate dataAplicacao,
                  String lote, String localAplicacao) {
        this.id = id;
        this.usuarioId = usuarioId;
        atualizar(nome, dataAplicacao, lote, localAplicacao);
    }

    public Long id() { return id; }
    public Long usuarioId() { return usuarioId; }
    public String nome() { return nome; }
    public LocalDate dataAplicacao() { return dataAplicacao; }
    public String lote() { return lote; }
    public String localAplicacao() { return localAplicacao; }

    public void atualizar(String nome, LocalDate dataAplicacao, String lote, String localAplicacao) {
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome da vacina é obrigatório.");
        if (dataAplicacao == null) throw new IllegalArgumentException("Data de aplicação é obrigatória.");
        if (dataAplicacao.isAfter(LocalDate.now())) throw new IllegalArgumentException("A data de aplicação não pode ser futura.");
        this.nome = nome.trim();
        this.dataAplicacao = dataAplicacao;
        this.lote = normalizar(lote);
        this.localAplicacao = normalizar(localAplicacao);
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
