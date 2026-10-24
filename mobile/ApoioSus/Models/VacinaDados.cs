namespace ApoioSus.Models;

// Vacina como a API devolve.
public record VacinaDados(int Id, string Nome, DateOnly DataAplicacao, string? Lote, string? LocalAplicacao);

// Vacina como a API recebe (cadastro e edição).
public record VacinaRequest(string Nome, DateOnly DataAplicacao, string? Lote, string? LocalAplicacao);
