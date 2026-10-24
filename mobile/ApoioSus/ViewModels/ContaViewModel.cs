using ApoioSus.Models;
using ApoioSus.Services;
using ApoioSus.Services.Api;
using ApoioSus.Services.Auth;

namespace ApoioSus.ViewModels;

public class ContaViewModel : ViewModelBase
{
	private readonly ApiService _api;
	private readonly SessaoUsuario _sessao;

	public ContaViewModel(ApiService api, SessaoUsuario sessao)
	{
		_api = api;
		_sessao = sessao;
		SalvarCommand = new Comando(SalvarAsync, () => !Carregando);
		ExcluirCommand = new Comando(ExcluirAsync, () => !Carregando);
		VoltarCommand = new Comando(() => Shell.Current.GoToAsync(".."));
	}

	public Comando SalvarCommand { get; }
	public Comando ExcluirCommand { get; }
	public Comando VoltarCommand { get; }

	private string _nome = "";
	public string Nome { get => _nome; set => SetProperty(ref _nome, value); }

	// O e-mail identifica a conta e só é exibido.
	private string _email = "";
	public string Email { get => _email; set => SetProperty(ref _email, value); }

	private string _dataNascimento = "";
	public string DataNascimento { get => _dataNascimento; set => SetProperty(ref _dataNascimento, value); }

	private string _nomeErro = "";
	public string NomeErro { get => _nomeErro; set => SetProperty(ref _nomeErro, value); }

	private string _dataNascimentoErro = "";
	public string DataNascimentoErro { get => _dataNascimentoErro; set => SetProperty(ref _dataNascimentoErro, value); }

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
			{
				SalvarCommand.AtualizarPodeExecutar();
				ExcluirCommand.AtualizarPodeExecutar();
			}
		}
	}

	// Chamado quando a tela aparece: busca os dados atuais da conta.
	public async Task CarregarAsync()
	{
		NomeErro = DataNascimentoErro = ErroGeral = Sucesso = "";
		Carregando = true;
		try
		{
			var usuario = await _api.GetAsync<UsuarioResponse>("/api/usuarios/me");
			Nome = usuario!.Nome;
			Email = usuario.Email;
			DataNascimento = Datas.ParaTela(usuario.DataNascimento);
		}
		catch (ApiException e) when (e.StatusCode != 401)
		{
			ErroGeral = e.Message;
		}
		catch (ApiException)
		{
			// 401: o evento SessaoUsuario.Expirada leva ao login.
		}
		finally
		{
			Carregando = false;
		}
	}

	private async Task SalvarAsync()
	{
		NomeErro = DataNascimentoErro = ErroGeral = Sucesso = "";

		if (string.IsNullOrWhiteSpace(Nome))
			NomeErro = "Informe o nome.";
		else if (Nome.Trim().Length > 120)
			NomeErro = "O nome deve ter no máximo 120 caracteres.";

		DateOnly? nascimento = null;
		if (!string.IsNullOrWhiteSpace(DataNascimento))
		{
			if (!Datas.TentarLer(DataNascimento, out var data))
				DataNascimentoErro = "Use o formato dd/mm/aaaa.";
			else if (Datas.EhFutura(data))
				DataNascimentoErro = "A data de nascimento não pode ser futura.";
			else
				nascimento = data;
		}

		if (NomeErro != "" || DataNascimentoErro != "")
			return;

		Carregando = true;
		try
		{
			var usuario = await _api.PutAsync<UsuarioResponse>("/api/usuarios/me",
				new AtualizarContaRequest(Nome.Trim(), nascimento));
			Nome = usuario!.Nome;
			DataNascimento = Datas.ParaTela(usuario.DataNascimento);
			Sucesso = "Dados salvos.";
		}
		catch (ApiException e)
		{
			if (e.Codigo == "VALIDACAO")
			{
				NomeErro = e.Erros.GetValueOrDefault("nome", "");
				DataNascimentoErro = e.Erros.GetValueOrDefault("dataNascimento", "");
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

	private async Task ExcluirAsync()
	{
		bool confirmou = await Shell.Current.DisplayAlertAsync("Excluir conta",
			"Todos os seus dados serão apagados. Esta ação não pode ser desfeita.", "Excluir", "Cancelar");
		if (!confirmou)
			return;

		Carregando = true;
		try
		{
			await _api.DeleteAsync("/api/usuarios/me");
			_sessao.Limpar();
			await Shell.Current.GoToAsync("//login");
		}
		catch (ApiException e)
		{
			ErroGeral = e.Message;
		}
		finally
		{
			Carregando = false;
		}
	}
}
