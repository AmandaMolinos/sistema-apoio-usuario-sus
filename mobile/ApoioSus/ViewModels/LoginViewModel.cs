using System.Text.RegularExpressions;
using ApoioSus.Models;
using ApoioSus.Services.Api;
using ApoioSus.Services.Auth;

namespace ApoioSus.ViewModels;

public class LoginViewModel : ViewModelBase
{
	private static readonly Regex EmailRegex = new(@"^[^@\s]+@[^@\s]+\.[^@\s]+$");

	private readonly ApiService _api;
	private readonly SessaoUsuario _sessao;

	public LoginViewModel(ApiService api, SessaoUsuario sessao)
	{
		_api = api;
		_sessao = sessao;
		EntrarCommand = new Comando(EntrarAsync, () => !Carregando);
		AlternarSenhaCommand = new Comando(() => { MostrarSenha = !MostrarSenha; return Task.CompletedTask; });
		IrParaCadastroCommand = new Comando(() => Shell.Current.GoToAsync("cadastro"));
		IrParaRecuperarSenhaCommand = new Comando(() => Shell.Current.GoToAsync("recuperar-senha"));
	}

	public Comando EntrarCommand { get; }
	public Comando IrParaCadastroCommand { get; }
	public Comando IrParaRecuperarSenhaCommand { get; }
	public Comando AlternarSenhaCommand { get; }

	// O olho do campo de senha mostra ou esconde o que foi digitado.
	private bool _mostrarSenha;
	public bool MostrarSenha
	{
		get => _mostrarSenha;
		set
		{
			if (SetProperty(ref _mostrarSenha, value))
			{
				Notificar(nameof(SenhaOculta));
				Notificar(nameof(IconeOlho));
			}
		}
	}
	public bool SenhaOculta => !MostrarSenha;
	public string IconeOlho => MostrarSenha ? "ic_olho_riscado.png" : "ic_olho.png";

	private string _email = "";
	public string Email { get => _email; set => SetProperty(ref _email, value); }

	private string _senha = "";
	public string Senha { get => _senha; set => SetProperty(ref _senha, value); }

	private string _emailErro = "";
	public string EmailErro { get => _emailErro; set => SetProperty(ref _emailErro, value); }

	private string _senhaErro = "";
	public string SenhaErro { get => _senhaErro; set => SetProperty(ref _senhaErro, value); }

	private string _erroGeral = "";
	public string ErroGeral { get => _erroGeral; set => SetProperty(ref _erroGeral, value); }

	private bool _carregando;
	public bool Carregando
	{
		get => _carregando;
		set
		{
			if (SetProperty(ref _carregando, value))
				EntrarCommand.AtualizarPodeExecutar();
		}
	}

	// Chamado ao exibir a tela: se há token salvo e ainda válido, entra direto.
	public async Task RestaurarSessaoAsync()
	{
		if (!_sessao.TemSessao)
			await _sessao.CarregarAsync();
		if (!_sessao.TemSessao)
			return;

		try
		{
			await _api.GetAsync<UsuarioResponse>("/api/usuarios/me");
			await Shell.Current.GoToAsync("//inicio");
		}
		catch (ApiException e) when (e.StatusCode == 401)
		{
			_sessao.Limpar();
		}
		catch (ApiException)
		{
			// Sem rede ou erro do servidor: permanece no login, o token salvo é mantido.
		}
	}

	public void LimparCampos()
	{
		Senha = "";
		MostrarSenha = false;
		EmailErro = SenhaErro = ErroGeral = "";
	}

	public void MostrarAviso() => ErroGeral = _sessao.ConsumirAviso() ?? ErroGeral;

	private async Task EntrarAsync()
	{
		EmailErro = SenhaErro = ErroGeral = "";

		if (string.IsNullOrWhiteSpace(Email))
			EmailErro = "Informe o e-mail.";
		else if (!EmailRegex.IsMatch(Email.Trim()))
			EmailErro = "E-mail inválido.";
		if (string.IsNullOrEmpty(Senha))
			SenhaErro = "Informe a senha.";
		if (EmailErro != "" || SenhaErro != "")
			return;

		Carregando = true;
		try
		{
			var resposta = await _api.PostAsync<LoginResponse>("/api/auth/login",
				new LoginRequest(Email.Trim(), Senha));
			await _sessao.SalvarAsync(resposta!.Token);
			Senha = "";
			await Shell.Current.GoToAsync("//inicio");
		}
		catch (ApiException e)
		{
			if (e.Codigo == "VALIDACAO")
			{
				EmailErro = e.Erros.GetValueOrDefault("email", "");
				SenhaErro = e.Erros.GetValueOrDefault("senha", "");
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
