using System.Text.RegularExpressions;
using ApoioSus.Models;
using ApoioSus.Services;
using ApoioSus.Services.Api;

namespace ApoioSus.ViewModels;

public class CadastroViewModel : ViewModelBase
{
	private static readonly Regex EmailRegex = new(@"^[^@\s]+@[^@\s]+\.[^@\s]+$");

	private readonly ApiService _api;

	public CadastroViewModel(ApiService api)
	{
		_api = api;
		CadastrarCommand = new Comando(CadastrarAsync, () => !Carregando);
		VoltarCommand = new Comando(() => Shell.Current.GoToAsync(".."));
		AlternarSenhaCommand = new Comando(() => { MostrarSenha = !MostrarSenha; return Task.CompletedTask; });
		AlternarConfirmacaoCommand = new Comando(() => { MostrarConfirmacao = !MostrarConfirmacao; return Task.CompletedTask; });
		AbrirTermosCommand = new Comando(() => Shell.Current.GoToAsync("documento?tipo=termos"));
		AbrirPrivacidadeCommand = new Comando(() => Shell.Current.GoToAsync("documento?tipo=privacidade"));
	}

	public Comando CadastrarCommand { get; }
	public Comando VoltarCommand { get; }
	public Comando AlternarSenhaCommand { get; }
	public Comando AlternarConfirmacaoCommand { get; }
	public Comando AbrirTermosCommand { get; }
	public Comando AbrirPrivacidadeCommand { get; }

	// Olhos dos campos de senha: mostram ou escondem o que foi digitado.
	private bool _mostrarSenha;
	public bool MostrarSenha
	{
		get => _mostrarSenha;
		set
		{
			if (SetProperty(ref _mostrarSenha, value))
			{
				Notificar(nameof(SenhaOculta));
				Notificar(nameof(IconeOlhoSenha));
			}
		}
	}
	public bool SenhaOculta => !MostrarSenha;
	public string IconeOlhoSenha => MostrarSenha ? "ic_olho_riscado.png" : "ic_olho.png";

	private bool _mostrarConfirmacao;
	public bool MostrarConfirmacao
	{
		get => _mostrarConfirmacao;
		set
		{
			if (SetProperty(ref _mostrarConfirmacao, value))
			{
				Notificar(nameof(ConfirmacaoOculta));
				Notificar(nameof(IconeOlhoConfirmacao));
			}
		}
	}
	public bool ConfirmacaoOculta => !MostrarConfirmacao;
	public string IconeOlhoConfirmacao => MostrarConfirmacao ? "ic_olho_riscado.png" : "ic_olho.png";

	// "Li e concordo com os Termos de Uso e a Política de Privacidade"
	private bool _aceitouTermos;
	public bool AceitouTermos { get => _aceitouTermos; set => SetProperty(ref _aceitouTermos, value); }

	private string _termosErro = "";
	public string TermosErro { get => _termosErro; set => SetProperty(ref _termosErro, value); }

	// Campos do formulário
	private string _nome = "";
	public string Nome { get => _nome; set => SetProperty(ref _nome, value); }

	private string _email = "";
	public string Email { get => _email; set => SetProperty(ref _email, value); }

	private string _senha = "";
	public string Senha { get => _senha; set => SetProperty(ref _senha, value); }

	private string _confirmarSenha = "";
	public string ConfirmarSenha { get => _confirmarSenha; set => SetProperty(ref _confirmarSenha, value); }

	private string _dataNascimento = "";
	public string DataNascimento { get => _dataNascimento; set => SetProperty(ref _dataNascimento, value); }

	// Mensagens de erro de cada campo
	private string _nomeErro = "";
	public string NomeErro { get => _nomeErro; set => SetProperty(ref _nomeErro, value); }

	private string _emailErro = "";
	public string EmailErro { get => _emailErro; set => SetProperty(ref _emailErro, value); }

	private string _senhaErro = "";
	public string SenhaErro { get => _senhaErro; set => SetProperty(ref _senhaErro, value); }

	private string _confirmarSenhaErro = "";
	public string ConfirmarSenhaErro { get => _confirmarSenhaErro; set => SetProperty(ref _confirmarSenhaErro, value); }

	private string _dataNascimentoErro = "";
	public string DataNascimentoErro { get => _dataNascimentoErro; set => SetProperty(ref _dataNascimentoErro, value); }

	private string _erroGeral = "";
	public string ErroGeral { get => _erroGeral; set => SetProperty(ref _erroGeral, value); }

	private bool _carregando;
	public bool Carregando
	{
		get => _carregando;
		set
		{
			if (SetProperty(ref _carregando, value))
				CadastrarCommand.AtualizarPodeExecutar();
		}
	}

	private async Task CadastrarAsync()
	{
		LimparErros();
		DateOnly? nascimento = ValidarFormulario();
		if (TemErro())
			return;

		Carregando = true;
		try
		{
			await _api.PostAsync<UsuarioResponse>("/api/auth/cadastro",
				new CadastroRequest(Nome.Trim(), Email.Trim(), Senha, nascimento));

			await Shell.Current.DisplayAlertAsync("Conta criada", "Seu cadastro foi realizado com sucesso.", "OK");
			await Shell.Current.GoToAsync("..");
		}
		catch (ApiException e)
		{
			MostrarErroDaApi(e);
		}
		finally
		{
			Carregando = false;
		}
	}

	// Valida os campos no aparelho (a API valida de novo). Devolve a data de nascimento, se informada.
	private DateOnly? ValidarFormulario()
	{
		DateOnly? nascimento = null;

		if (string.IsNullOrWhiteSpace(Nome))
			NomeErro = "Informe o nome.";
		else if (Nome.Trim().Length > 120)
			NomeErro = "O nome deve ter no máximo 120 caracteres.";

		if (string.IsNullOrWhiteSpace(Email))
			EmailErro = "Informe o e-mail.";
		else if (!EmailRegex.IsMatch(Email.Trim()))
			EmailErro = "E-mail inválido.";

		if (string.IsNullOrEmpty(Senha))
			SenhaErro = "Informe a senha.";
		else if (Senha.Length < 8 || Senha.Length > 72)
			SenhaErro = "A senha deve ter entre 8 e 72 caracteres.";

		if (ConfirmarSenha != Senha)
			ConfirmarSenhaErro = "As senhas não coincidem.";

		if (!string.IsNullOrWhiteSpace(DataNascimento))
		{
			if (!Datas.TentarLer(DataNascimento, out var data))
				DataNascimentoErro = "Use o formato dd/mm/aaaa.";
			else if (Datas.EhFutura(data))
				DataNascimentoErro = "A data de nascimento não pode ser futura.";
			else
				nascimento = data;
		}

		if (!AceitouTermos)
			TermosErro = "Para criar a conta, aceite os Termos de Uso e a Política de Privacidade.";

		return nascimento;
	}

	private bool TemErro()
		=> NomeErro != "" || EmailErro != "" || SenhaErro != "" || ConfirmarSenhaErro != "" || DataNascimentoErro != ""
			|| TermosErro != "";

	// Erros de validação do servidor chegam por campo (nome, email, senha, dataNascimento).
	private void MostrarErroDaApi(ApiException e)
	{
		if (e.Codigo == "EMAIL_JA_CADASTRADO")
		{
			EmailErro = e.Message;
		}
		else if (e.Codigo == "VALIDACAO")
		{
			NomeErro = e.Erros.GetValueOrDefault("nome", "");
			EmailErro = e.Erros.GetValueOrDefault("email", "");
			SenhaErro = e.Erros.GetValueOrDefault("senha", "");
			DataNascimentoErro = e.Erros.GetValueOrDefault("dataNascimento", "");
			if (!TemErro())
				ErroGeral = e.Message;
		}
		else
		{
			ErroGeral = e.Message;
		}
	}

	private void LimparErros()
	{
		NomeErro = EmailErro = SenhaErro = ConfirmarSenhaErro = DataNascimentoErro = TermosErro = ErroGeral = "";
	}
}
