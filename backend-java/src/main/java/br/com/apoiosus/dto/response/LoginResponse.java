package br.com.apoiosus.dto.response;

// token, tipo e expiresIn (segundos) são o formato do login do backend Spring do projeto.
// expiraEm (data e hora) e usuario são extras nossos que o app usa.
public record LoginResponse(String token, String tipo, long expiresIn, String expiraEm, UsuarioResponse usuario) {
}
