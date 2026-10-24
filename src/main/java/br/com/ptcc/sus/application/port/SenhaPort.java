package br.com.ptcc.sus.application.port;

public interface SenhaPort {
    String hash(String senha);

    boolean corresponde(String senha, String hash);
}