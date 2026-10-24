using ApoioSus.Models;
using ApoioSus.Services;
using ApoioSus.Services.Api;

namespace ApoioSus.ViewModels;

public class PerfilSaudeViewModel : ViewModelBase
{
	private const string Rota = "/api/perfil";
	private const string NaoInformado = "Não informado";

	private readonly ApiService _api;

	public PerfilSaudeViewModel(ApiService api)
	{
		_api = api;
		SalvarCommand = new Comando(SalvarAsync, () => !Carregando);
		LimparCommand = new Comando(LimparAsync, () => !Carregando);
		VoltarCommand = new Comando(() => Shell.Current.GoToAsync(".."));
		EditarContaCommand = new Comando(() => Shell.Current.GoToAsync("conta"));
		EditarSaudeCommand = new Comando(() => { Editando = true; return Task.CompletedTask; });
		CancelarCommand = new Comando(CancelarAsync);
	}

	public Comando SalvarCommand { get; }
	public Comando LimparCommand { get; }
	public Comando VoltarCommand { get; }
	public Comando EditarContaCommand { get; }
	public Comando EditarSaudeCommand { get; }
	public Comando CancelarCommand { get; }

	// A tela abre só para leitura; "Editar" mostra o formulário.
	private bool _editando;
	public bool Editando
	{
		get => _editando;
		set
		{
			if (SetProperty(ref _editando, value))
				Notificar(nameof(Visualizando));
		}
	}
	public bool Visualizando => !Editando;

	// Dados pessoais (somente leitura; a edição fica em Minha conta)
	private string _nome = "";
	public string Nome { get => _nome; set => SetProperty(ref _nome, value); }

	private string _dataNascimento = "";
	public string DataNascimento { get => _dataNascimento; set => SetProperty(ref _dataNascimento, value); }

	private string _email = "";
	public string Email { get => _email; set => SetProperty(ref _email, value); }

	// Textos da visualização
	private string _pesoTexto = "";
	public string PesoTexto { get => _pesoTexto; set => SetProperty(ref _pesoTexto, value); }

	private string _alturaTexto = "";
	public string AlturaTexto { get => _alturaTexto; set => SetProperty(ref _alturaTexto, value); }

	private string _tipoTexto = "";
	public string TipoTexto { get => _tipoTexto; set => SetProperty(ref _tipoTexto, value); }

	private string _alergiasTexto = "";
	public string AlergiasTexto { get => _alergiasTexto; set => SetProperty(ref _alergiasTexto, value); }

	private string _observacoesTexto = "";
	public string ObservacoesTexto { get => _observacoesTexto; set => SetProperty(ref _observacoesTexto, value); }

	// Opções da lista de tipo sanguíneo
	public IReadOnlyList<string> Tipos { get; } =
		new[] { NaoInformado, "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-" };

	private string _peso = "";
	public string Peso { get => _peso; set => SetProperty(ref _peso, value); }

	private string _altura = "";
	public string Altura { get => _altura; set => SetProperty(ref _altura, value); }

	private string _tipoSanguineo = NaoInformado;
	public string TipoSanguineo { get => _tipoSanguineo; set => SetProperty(ref _tipoSanguineo, value ?? NaoInformado); }

	private string _alergias = "";
	public string Alergias { get => _alergias; set => SetProperty(ref _alergias, value); }

	private string _observacoes = "";
	public string Observacoes { get => _observacoes; set => SetProperty(ref _observacoes, value); }

	private string _pesoErro = "";
	public string PesoErro { get => _pesoErro; set => SetProperty(ref _pesoErro, value); }

	private string _alturaErro = "";
	public string AlturaErro { get => _alturaErro; set => SetProperty(ref _alturaErro, value); }

	private string _alergiasErro = "";
	public string AlergiasErro { get => _alergiasErro; set => SetProperty(ref _alergiasErro, value); }

	private string _observacoesErro = "";
	public string ObservacoesErro { get => _observacoesErro; set => SetProperty(ref _observacoesErro, value); }

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
				LimparCommand.AtualizarPodeExecutar();
			}
		}
	}

	// Chamado quando a tela aparece: busca o perfil atual.
	public async Task CarregarAsync()
	{
		LimparMensagens();
		Carregando = true;
		try
		{
			Editando = false;
			var usuario = await _api.GetAsync<UsuarioResponse>("/api/usuarios/me");
			Nome = usuario!.Nome;
			Email = usuario.Email;
			DataNascimento = Datas.ParaTela(usuario.DataNascimento);

			var perfil = await _api.GetAsync<PerfilSaudeDados>(Rota);
			PreencherTela(perfil!);
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
		LimparMensagens();

		double? peso = null;
		if (!string.IsNullOrWhiteSpace(Peso))
		{
			if (!Numeros.TentarLer(Peso, out var valor))
				PesoErro = "Informe um número. Exemplo: 72,5";
			else if (valor <= 0 || valor > 500)
				PesoErro = "O peso deve ser maior que zero e no máximo 500 kg.";
			else
				peso = valor;
		}

		double? altura = null;
		if (!string.IsNullOrWhiteSpace(Altura))
		{
			if (!Numeros.TentarLer(Altura, out var valor))
				AlturaErro = "Informe um número. Exemplo: 1,75";
			else if (valor <= 0 || valor > 3)
				AlturaErro = "A altura deve ser maior que zero e no máximo 3 m.";
			else
				altura = valor;
		}

		if (Alergias.Length > 500)
			AlergiasErro = "As alergias devem ter no máximo 500 caracteres.";
		if (Observacoes.Length > 1000)
			ObservacoesErro = "As observações devem ter no máximo 1000 caracteres.";

		if (PesoErro != "" || AlturaErro != "" || AlergiasErro != "" || ObservacoesErro != "")
			return;

		Carregando = true;
		try
		{
			string? tipo = TipoSanguineo == NaoInformado ? null : TipoSanguineo;
			var salvo = await _api.PutAsync<PerfilSaudeDados>(Rota,
				new PerfilSaudeDados(peso, altura, tipo, Alergias, Observacoes));
			PreencherTela(salvo!);
			Editando = false;
			Sucesso = "Perfil salvo.";
		}
		catch (ApiException e)
		{
			if (e.Codigo == "VALIDACAO")
			{
				PesoErro = e.Erros.GetValueOrDefault("peso", "");
				AlturaErro = e.Erros.GetValueOrDefault("altura", "");
				AlergiasErro = e.Erros.GetValueOrDefault("alergias", "");
				ObservacoesErro = e.Erros.GetValueOrDefault("observacoesMedicas", "");
				ErroGeral = e.Erros.GetValueOrDefault("tipoSanguineo", "");
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

	private async Task LimparAsync()
	{
		bool confirmou = await Shell.Current.DisplayAlertAsync("Limpar perfil",
			"Peso, altura, tipo sanguíneo, alergias e observações serão apagados.", "Limpar", "Cancelar");
		if (!confirmou)
			return;

		LimparMensagens();
		Carregando = true;
		try
		{
			await _api.DeleteAsync(Rota);
			PreencherTela(new PerfilSaudeDados(null, null, null, null, null));
			Sucesso = "Perfil limpo.";
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

	private void PreencherTela(PerfilSaudeDados perfil)
	{
		Peso = Numeros.ParaTela(perfil.Peso);
		Altura = Numeros.ParaTela(perfil.Altura);
		TipoSanguineo = perfil.TipoSanguineo ?? NaoInformado;
		Alergias = perfil.Alergias ?? "";
		Observacoes = perfil.ObservacoesMedicas ?? "";

		PesoTexto = perfil.Peso is null ? NaoInformado : $"{Numeros.ParaTela(perfil.Peso)} kg";
		AlturaTexto = perfil.Altura is null ? NaoInformado : $"{Numeros.ParaTela(perfil.Altura)} m";
		TipoTexto = perfil.TipoSanguineo ?? NaoInformado;
		AlergiasTexto = string.IsNullOrWhiteSpace(perfil.Alergias) ? "Nenhuma informada" : perfil.Alergias;
		ObservacoesTexto = string.IsNullOrWhiteSpace(perfil.ObservacoesMedicas) ? "Nenhuma observação" : perfil.ObservacoesMedicas;
	}

	// Descarta o que foi digitado e volta a mostrar os dados salvos.
	private async Task CancelarAsync()
	{
		await CarregarAsync();
	}

	private void LimparMensagens()
	{
		PesoErro = AlturaErro = AlergiasErro = ObservacoesErro = ErroGeral = Sucesso = "";
	}
}
