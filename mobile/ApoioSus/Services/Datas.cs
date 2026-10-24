using System.Globalization;

namespace ApoioSus.Services;

// A tela usa dd/mm/aaaa; a API usa aaaa-mm-dd. Estas funções fazem a conversão.
public static class Datas
{
	private static readonly CultureInfo PtBr = new("pt-BR");

	public static string ParaTela(DateOnly? data) => data?.ToString("dd/MM/yyyy", PtBr) ?? "";

	// Lê um texto no formato dd/mm/aaaa. Retorna false se não for uma data válida.
	public static bool TentarLer(string texto, out DateOnly data)
		=> DateOnly.TryParseExact(texto.Trim(), "dd/MM/yyyy", PtBr, DateTimeStyles.None, out data);

	public static bool EhFutura(DateOnly data) => data > DateOnly.FromDateTime(DateTime.Today);
}
