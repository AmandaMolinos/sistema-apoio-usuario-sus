using ApoioSus.Services.Api;
using ApoioSus.Services.Auth;

namespace ApoioSus.ViewModels;

// Aba "Mais": conta do usuário e saída do app.
public class MaisViewModel : ViewModelBase
{
	private readonly ApiService _api;
	private readonly SessaoUsuario _sessao;

	public MaisViewModel(ApiService api, SessaoUsuario sessao)
	{
		_api = api;
		_sessao = sessao;
		ContaCommand = new Comando(() => Shell.Current.GoToAsync("conta"));
		SairCommand = new Comando(SairAsync);
	}

	public Comando ContaCommand { get; }
	public Comando SairCommand { get; }

	private async Task SairAsync()
	{
		try
		{
			await _api.PostAsync("/api/auth/logout", null);
		}
		catch (ApiException)
		{
			// Sem rede ou token já inválido: a sessão local é encerrada de qualquer forma.
		}

		_sessao.Limpar();
		await Shell.Current.GoToAsync("//login");
	}
}
