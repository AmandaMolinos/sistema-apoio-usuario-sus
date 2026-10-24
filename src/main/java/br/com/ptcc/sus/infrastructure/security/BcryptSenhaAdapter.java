package br.com.ptcc.sus.infrastructure.security;

import br.com.ptcc.sus.application.port.SenhaPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BcryptSenhaAdapter implements SenhaPort {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String hash(String s) {
        return encoder.encode(s);
    }

    public boolean corresponde(String s, String h) {
        return encoder.matches(s, h);
    }
}