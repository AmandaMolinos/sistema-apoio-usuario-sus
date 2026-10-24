namespace ApoioSus.Models;

// Medicamento como a API devolve.
public record MedicamentoDados(int Id, string Nome, string? Dosagem, string? Frequencia,
	DateOnly? DataInicio, DateOnly? DataFim);

// Medicamento como a API recebe (cadastro e edição).
public record MedicamentoRequest(string Nome, string? Dosagem, string? Frequencia,
	DateOnly? DataInicio, DateOnly? DataFim);
