package br.com.ptcc.sus.application.port;

public interface TokenPort {
    String gerar(String subject);

    String subject(String token);

    boolean valido(String token);

    long expiracaoSegundos();
}