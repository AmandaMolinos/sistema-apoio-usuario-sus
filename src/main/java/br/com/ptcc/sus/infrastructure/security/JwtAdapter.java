package br.com.ptcc.sus.infrastructure.security;

import br.com.ptcc.sus.application.port.TokenPort;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtAdapter implements TokenPort {
    @Value("${app.jwt.secret}")
    private String secret;
    @Value("${app.jwt.expiration-seconds:3600}")
    private long seconds;
    private SecretKey key;

    @PostConstruct
    void init() {
        if (secret == null || secret.isBlank())
            throw new IllegalStateException("Configure JWT_SECRET com chave Base64 de 32 bytes ou mais.");
        byte[] b = Base64.getDecoder().decode(secret);
        if (b.length < 32) throw new IllegalStateException("JWT_SECRET precisa ter pelo menos 32 bytes.");
        key = Keys.hmacShaKeyFor(b);
    }

    public String gerar(String subject) {
        Date now = new Date();
        return Jwts.builder().subject(subject).issuedAt(now).expiration(new Date(now.getTime() + seconds * 1000)).signWith(key).compact();
    }

    public String subject(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
    }

    public boolean valido(String token) {
        try {
            subject(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public long expiracaoSegundos() {
        return seconds;
    }
}