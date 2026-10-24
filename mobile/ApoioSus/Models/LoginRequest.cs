namespace ApoioSus.Models;

public record LoginRequest(string Email, string Senha);

public record LoginResponse(string Token, DateTime ExpiraEm, UsuarioResponse Usuario);
