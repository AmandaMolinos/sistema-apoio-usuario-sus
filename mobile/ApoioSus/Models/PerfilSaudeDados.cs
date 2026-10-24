namespace ApoioSus.Models;

// Peso em kg e altura em metros. Usado para enviar e para receber o perfil de saúde.
public record PerfilSaudeDados(double? Peso, double? Altura, string? TipoSanguineo, string? Alergias,
	string? ObservacoesMedicas);
