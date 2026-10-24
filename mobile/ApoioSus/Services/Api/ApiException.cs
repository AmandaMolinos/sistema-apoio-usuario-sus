namespace ApoioSus.Services.Api;

public class ApiException : Exception
{
	public int StatusCode { get; }
	public string Codigo { get; }
	public IReadOnlyDictionary<string, string> Erros { get; }

	public ApiException(int statusCode, string codigo, string mensagem, IReadOnlyDictionary<string, string>? erros = null)
		: base(mensagem)
	{
		StatusCode = statusCode;
		Codigo = codigo;
		Erros = erros ?? new Dictionary<string, string>();
	}
}
