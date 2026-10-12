package br.com.ptcc.sus.domain.model;

public class PerfilSaude {
    private final Long id;
    private final Long usuarioId;
    private Double peso;
    private Double altura;
    private String tipoSanguineo;
    private String alergias;
    private String observacoesMedicas;

    public PerfilSaude(Long id, Long usuarioId, Double peso, Double altura, String tipoSanguineo, String alergias, String observacoesMedicas) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.peso = peso;
        this.altura = altura;
        this.tipoSanguineo = tipoSanguineo;
        this.alergias = alergias;
        this.observacoesMedicas = observacoesMedicas;
    }

    public Long id() {
        return id;
    }

    public Long usuarioId() {
        return usuarioId;
    }

    public Double peso() {
        return peso;
    }

    public Double altura() {
        return altura;
    }

    public String tipoSanguineo() {
        return tipoSanguineo;
    }

    public String alergias() {
        return alergias;
    }

    public String observacoesMedicas() {
        return observacoesMedicas;
    }

    public void atualizar(Double peso, Double altura, String tipo, String alergias, String observacoes) {
        this.peso = peso;
        this.altura = altura;
        this.tipoSanguineo = tipo;
        this.alergias = alergias;
        this.observacoesMedicas = observacoes;
    }
}