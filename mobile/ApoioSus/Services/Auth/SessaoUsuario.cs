namespace ApoioSus.Services.Auth;

// Guarda o token da sessão: em memória durante o uso e no armazenamento seguro da plataforma.
public class SessaoUsuario
{
	private const string ChaveToken = "apoiosus_token";

	public string? Token { get; private set; }

	public bool TemSessao => !string.IsNullOrEmpty(Token);

	// Aviso a exibir na tela de login (ex.: sessão expirada). Lido uma única vez.
	private string? _aviso;

	// Disparado quando o servidor recusa o token (expirado ou revogado).
	public event Action? Expirada;

	public async Task CarregarAsync()
	{
		try
		{
			Token = await SecureStorage.Default.GetAsync(ChaveToken);
		}
		catch (Exception)
		{
			// Armazenamento seguro indisponível (ex.: app Windows sem pacote): segue sem sessão salva.
			Token = null;
		}
	}

	public async Task SalvarAsync(string token)
	{
		Token = token;
		try
		{
			await SecureStorage.Default.SetAsync(ChaveToken, token);
		}
		catch (Exception)
		{
			// Sem persistência: a sessão vale apenas até fechar o app.
		}
	}

	public void Limpar()
	{
		Token = null;
		try
		{
			SecureStorage.Default.Remove(ChaveToken);
		}
		catch (Exception) { }
	}

	public void Expirar()
	{
		Limpar();
		_aviso = "Sua sessão expirou. Entre novamente.";
		Expirada?.Invoke();
	}

	public string? ConsumirAviso()
	{
		var aviso = _aviso;
		_aviso = null;
		return aviso;
	}
}
