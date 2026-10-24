using ApoioSus.Models;
using ApoioSus.Services.Api;

namespace ApoioSus.ViewModels;

public class RedefinirSenhaViewModel : ViewModelBase
{
	private readonly ApiService _api;

	public RedefinirSenhaViewModel(ApiService api)
	{
		_api = api;
		SalvarCommand = new Comando(SalvarAsync, () => !Carregando);
		VoltarCommand = new Comando(() => Shell.Current.GoToAsync(".."));
	}

	public Comando SalvarCommand { get; }
	public Comando VoltarCommand { get; }

	private string _codigo = "";
	public string Codigo { get => _codigo; set => SetProperty(ref _codigo, value); }

	private string _novaSenha = "";
	public string NovaSenha { get => _novaSenha; set => SetProperty(ref _novaSenha, value); }

	private string _confirmarSenha = "";
	public string ConfirmarSenha { get => _confirmarSenha; set => SetProperty(ref _confirmarSenha, value); }

	private string _codigoErro = "";
	public string CodigoErro { get => _codigoErro; set => SetProperty(ref _codigoErro, value); }

	private string _novaSenhaErro = "";
	public string NovaSenhaErro { get => _novaSenhaErro; set => SetProperty(ref _novaSenhaErro, value); }

	private string _confirmarSenhaErro = "";
	public string ConfirmarSenhaErro { get => _confirmarSenhaErro; set => SetProperty(ref _confirmarSenhaErro, value); }

	private string _erroGeral = "";
	public string ErroGeral { get => _erroGeral; set => SetProperty(ref _erroGeral, value); }

	private bool _carregando;
	public bool Carregando
	{
		get => _carregando;
		set
		{
			if (SetProperty(ref _carregando, value))
				SalvarCommand.AtualizarPodeExecutar();
		}
	}

	private async Task SalvarAsync()
	{
		CodigoErro = NovaSenhaErro = ConfirmarSenhaErro = ErroGeral = "";

		if (string.IsNullOrWhiteSpace(Codigo))
			CodigoErro = "Informe o código.";
		if (string.IsNullOrEmpty(NovaSenha))
			NovaSenhaErro = "Informe a nova senha.";
		else if (NovaSenha.Length < 8 || NovaSenha.Length > 72)
			NovaSenhaErro = "A senha deve ter entre 8 e 72 caracteres.";
		if (ConfirmarSenha != NovaSenha)
			ConfirmarSenhaErro = "As senhas não coincidem.";
		if (CodigoErro != "" || NovaSenhaErro != "" || ConfirmarSenhaErro != "")
			return;

		Carregando = true;
		try
		{
			await _api.PostAsync<MensagemResponse>("/api/auth/redefinir-senha",
				new RedefinirSenhaRequest(Codigo.Trim(), NovaSenha));

			await Shell.Current.DisplayAlertAsync("Senha alterada", "Entre com a sua nova senha.", "OK");
			await Shell.Current.GoToAsync("//login");
		}
		catch (ApiException e)
		{
			if (e.Codigo == "CODIGO_INVALIDO")
			{
				CodigoErro = e.Message;
			}
			else if (e.Codigo == "VALIDACAO")
			{
				CodigoErro = e.Erros.GetValueOrDefault("codigo", "");
				NovaSenhaErro = e.Erros.GetValueOrDefault("novaSenha", "");
			}
			else
			{
				ErroGeral = e.Message;
			}
		}
		finally
		{
			Carregando = false;
		}
	}
}
