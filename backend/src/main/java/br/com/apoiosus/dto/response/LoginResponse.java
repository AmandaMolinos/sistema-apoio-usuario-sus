package br.com.apoiosus.dto.response;

import java.time.LocalDateTime;

public record LoginResponse(String token, LocalDateTime expiraEm, UsuarioResponse usuario) {
}
