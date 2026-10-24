package br.com.apoiosus.service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

// Senha nunca é salva em texto. Usamos PBKDF2 (já vem no JDK) com um "salt" aleatório por senha.
// Formato salvo: pbkdf2_sha256$<iteracoes>$<salt>$<hash>
public class SenhaService {

    private static final int ITERACOES = 210_000;
    private static final SecureRandom RANDOM = new SecureRandom();

    public String gerarHash(String senha) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        byte[] hash = derivar(senha, salt, ITERACOES);
        return "pbkdf2_sha256$" + ITERACOES + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(hash);
    }

    public boolean confere(String senha, String armazenado) {
        String[] partes = armazenado.split("\\$");
        if (partes.length != 4) {
            return false;
        }
        byte[] salt = Base64.getDecoder().decode(partes[2]);
        byte[] esperado = Base64.getDecoder().decode(partes[3]);
        byte[] calculado = derivar(senha, salt, Integer.parseInt(partes[1]));
        return MessageDigest.isEqual(esperado, calculado);
    }

    // SHA-256 em hexadecimal. Usado para guardar tokens no banco sem guardar o token em si.
    public static String sha256(String texto) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    private static byte[] derivar(String senha, byte[] salt, int iteracoes) {
        try {
            PBEKeySpec spec = new PBEKeySpec(senha.toCharArray(), salt, iteracoes, 256);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
