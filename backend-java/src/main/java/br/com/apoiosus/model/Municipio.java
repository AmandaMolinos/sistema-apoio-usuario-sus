package br.com.apoiosus.model;

// codigo = código IBGE com 6 dígitos (sem o dígito verificador), como o CNES espera.
public record Municipio(String codigo, String nome) {
}
