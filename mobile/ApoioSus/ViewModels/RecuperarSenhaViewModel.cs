using System.Text.RegularExpressions;
using ApoioSus.Models;
using ApoioSus.Services.Api;

namespace ApoioSus.ViewModels;

public class RecuperarSenhaViewModel : ViewModelBase
{
	private static readonly Regex EmailRegex = new(@"^[^@\s]+@[^@\s]+\.[^@\s]+$");

	private readonly ApiService _api;

	public RecuperarSenhaViewModel(ApiService api)
	{
		_api = api;
		EnviarCommand = new Comando(EnviarAsync, () => !Carregando);
		VoltarCommand = new Comando(() => Shell.Current.GoToAsync(".."));
		JaTenhoCodigoCommand = new Comando(() => Shell.Current.GoToAsync("redefinir-senha"));
	}

	public Comando EnviarCommand { get; }
	public Comando VoltarCommand { get; }
	public Comando JaTenhoCodigoCommand { get; }

	private string _email = "";
	public string Email { get => _email; set => SetProperty(ref _email, value); }

	private string _emailErro = "";
	public string EmailErro { get => _emailErro; set => SetProperty(ref _emailErro, value); }

	private string _erroGeral = "";
	public string ErroGeral { get => _erroGeral; set => SetProperty(ref _erroGeral, value); }

	private string _sucesso = "";
	public string Sucesso { get => _sucesso; set => SetProperty(ref _sucesso, value); }

	private bool _carregando;
	public bool Carregando
	{
		get => _carregando;
		set
		{
			if (SetProperty(ref _carregando, value))
				EnviarCommand.AtualizarPodeExecutar();
		}
	}

	private async Task EnviarAsync()
	{
		EmailErro = ErroGeral = Sucesso = "";

		if (string.IsNullOrWhiteSpace(Email))
			EmailErro = "Informe o e-mail.";
		else if (!EmailRegex.IsMatch(Email.Trim()))
			EmailErro = "E-mail inválido.";
		if (EmailErro != "")
			return;

		Carregando = true;
		try
		{
			var resposta = await _api.PostAsync<MensagemResponse>("/api/auth/recuperar-senha",
				new RecuperarSenhaRequest(Email.Trim()));
			Sucesso = resposta?.Mensagem ?? "Solicitação enviada.";
		}
		catch (ApiException e)
		{
			if (e.Codigo == "VALIDACAO")
				EmailErro = e.Erros.GetValueOrDefault("email", e.Message);
			else
				ErroGeral = e.Message;
		}
		finally
		{
			Carregando = false;
		}
	}
}
