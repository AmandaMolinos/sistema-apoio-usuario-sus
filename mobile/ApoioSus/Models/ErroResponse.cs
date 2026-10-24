namespace ApoioSus.Models;

public record ErroResponse(string Codigo, string Mensagem, Dictionary<string, string>? Erros);
