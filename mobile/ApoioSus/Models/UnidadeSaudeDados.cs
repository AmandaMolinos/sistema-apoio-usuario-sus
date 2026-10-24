namespace ApoioSus.Models;

// Município do Estado de São Paulo. Codigo é o código IBGE de 6 dígitos.
public record MunicipioDados(string Codigo, string Nome);

// Unidade de saúde como a API devolve (dados do CNES).
public record UnidadeDados(string? Nome, string? Tipo, string? Logradouro, string? Numero, string? Bairro,
	string? Cep, string? Municipio, string? Uf, List<string>? Telefones, string? Email, bool AtendeSus,
	DateOnly? AtualizadoEm);

// Uma página de UPAs. Proximo é o "inicio" da página seguinte; null quando não há mais.
public record PaginaUnidades(List<UnidadeDados>? Itens, int? Proximo);
