using ApoioSus.Models;
using ApoioSus.Services.Api;

namespace ApoioSus.ViewModels;

// Tela inicial: saúda o usuário e leva às principais áreas do app.
public class InicioViewModel : ViewModelBase
{
	private readonly ApiService _api;

	public InicioViewModel(ApiService api)
	{
		_api = api;
		PerfilSaudeCommand = new Comando(() => Shell.Current.GoToAsync("perfil-saude"));
		VacinasCommand = new Comando(() => Shell.Current.GoToAsync("//vacinas"));
		MedicamentosCommand = new Comando(() => Shell.Current.GoToAsync("//medicamentos"));
		UnidadeSaudeCommand = new Comando(() => Shell.Current.GoToAsync("//unidade-saude"));
		OrientacoesCommand = new Comando(() => Shell.Current.GoToAsync("orientacoes"));
		SintomasCommand = new Comando(() => Shell.Current.GoToAsync("sintomas"));
	}

	public Comando SintomasCommand { get; }
	public Comando OrientacoesCommand { get; }
	public Comando PerfilSaudeCommand { get; }
	public Comando VacinasCommand { get; }
	public Comando MedicamentosCommand { get; }
	public Comando UnidadeSaudeCommand { get; }

	private string _saudacao = "";
	public string Saudacao { get => _saudacao; set => SetProperty(ref _saudacao, value); }

	private string _erroGeral = "";
	public string ErroGeral { get => _erroGeral; set => SetProperty(ref _erroGeral, value); }

	public async Task CarregarUsuarioAsync()
	{
		ErroGeral = "";
		try
		{
			var usuario = await _api.GetAsync<UsuarioResponse>("/api/usuarios/me");
			Saudacao = $"Olá, {PrimeiroNome(usuario?.Nome)}!";
		}
		catch (ApiException e) when (e.StatusCode != 401)
		{
			Saudacao = "";
			ErroGeral = e.Message;
		}
		catch (ApiException)
		{
			// 401: o evento SessaoUsuario.Expirada leva ao login.
		}
	}

	// "Maria Silva" -> "Maria"
	private static string PrimeiroNome(string? nomeCompleto)
		=> nomeCompleto?.Split(' ', StringSplitOptions.RemoveEmptyEntries).FirstOrDefault() ?? "";
}
