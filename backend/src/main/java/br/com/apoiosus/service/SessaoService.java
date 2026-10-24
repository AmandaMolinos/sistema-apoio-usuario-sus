package br.com.apoiosus.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.apoiosus.model.entity.Sessao;
import br.com.apoiosus.repository.SessaoRepository;

@Service
public class SessaoService {

    public record NovaSessao(String token, LocalDateTime expiraEm) {
    }

    private static final SecureRandom RANDOM = new SecureRandom();

    private final SessaoRepository sessoes;
    private final long horasValidade;

    public SessaoService(SessaoRepository sessoes, @Value("${app.sessao.horas-validade}") long horasValidade) {
        this.sessoes = sessoes;
        this.horasValidade = horasValidade;
    }

    // Token opaco aleatório: devolvido uma única vez ao cliente; no banco fica só o hash SHA-256.
    @Transactional
    public NovaSessao criar(Integer usuarioId) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime expiraEm = agora.plusHours(horasValidade);
        sessoes.save(new Sessao(usuarioId, hash(token), agora, expiraEm));
        return new NovaSessao(token, expiraEm);
    }

    @Transactional(readOnly = true)
    public Optional<Sessao> buscarValida(String token) {
        return sessoes.findByTokenHash(hash(token)).filter(s -> s.estaValida(LocalDateTime.now()));
    }

    @Transactional
    public void revogar(Integer sessaoId) {
        sessoes.findById(sessaoId).ifPresent(s -> s.revogar(LocalDateTime.now()));
    }

    public static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
