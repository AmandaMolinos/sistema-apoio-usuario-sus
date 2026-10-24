using System.Globalization;

namespace ApoioSus.Services;

// Na tela o decimal usa vírgula (72,5); na API usa ponto. Estas funções fazem a conversão.
public static class Numeros
{
	private static readonly CultureInfo PtBr = new("pt-BR");

	public static string ParaTela(double? valor) => valor?.ToString("0.##", PtBr) ?? "";

	// Aceita "72,5" ou "72.5". Retorna false se não for um número.
	public static bool TentarLer(string texto, out double valor)
		=> double.TryParse(texto.Trim().Replace(',', '.'), NumberStyles.Float, CultureInfo.InvariantCulture, out valor);
}
