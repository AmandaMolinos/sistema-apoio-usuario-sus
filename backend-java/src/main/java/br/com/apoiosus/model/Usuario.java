package br.com.apoiosus.model;

public record Usuario(int id, String nome, String email, String senhaHash, String dataNascimento) {
}
