namespace ApoioSus.Models;

public record CadastroRequest(string Nome, string Email, string Senha, DateOnly? DataNascimento);

public record UsuarioResponse(int Id, string Nome, string Email, DateOnly? DataNascimento);
