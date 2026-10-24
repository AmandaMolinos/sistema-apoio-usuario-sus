using System.Collections.ObjectModel;
using ApoioSus.Models;
using ApoioSus.Services.Api;

namespace ApoioSus.ViewModels;

// Um sintoma reconhecido, com as orientações já em texto de lista ("• item").
public class SintomaItem
{
	public string Nome { get; }
	public string Cuidados { get; }
	public string Procurar { get; }

	public SintomaItem(SintomaResultado sintoma)
	{
		Nome = sintoma.Nome;
		Cuidados = Marcadores(sintoma.Cuidados);
		Procurar = Marcadores(sintoma.Procurar);
	}

	private static string Marcadores(List<string>? itens)
		=> string.Join("\n", (itens ?? new List<string>()).Select(i => "• " + i));
}

public class SintomasViewModel : ViewModelBase
{
	private const int Maximo = 500;
	private const string AvisoPadrao = "Esta funcionalidade oferece apenas orientações informativas e não substitui a avaliação de um profissional de saúde.";

	private readonly ApiService _api;

	public SintomasViewModel(ApiService api)
	{
		_api = api;
		AnalisarCommand = new Comando(AnalisarAsync, () => !Carregando);
		NovaAnaliseCommand = new Comando(NovaAnalise);
		VoltarCommand = new Comando(() => Shell.Current.GoToAsync(".."));
		VerUnidadesCommand = new Comando(() => Shell.Current.GoToAsync("//unidade-saude"));
	}

	public Comando AnalisarCommand { get; }
	public Comando NovaAnaliseCommand { get; }
	public Comando VoltarCommand { get; }
	public Comando VerUnidadesCommand { get; }

	public ObservableCollection<SintomaItem> Sintomas { get; } = new();

	private string _texto = "";
	public string Texto { get => _texto; set => SetProperty(ref _texto, value ?? ""); }

	private string _textoErro = "";
	public string TextoErro { get => _textoErro; set => SetProperty(ref _textoErro, value); }

	private string _erroGeral = "";
	public string ErroGeral { get => _erroGeral; set => SetProperty(ref _erroGeral, value); }

	private string _aviso = AvisoPadrao;
	public string Aviso { get => _aviso; private set => SetProperty(ref _aviso, value); }

	private string _alertas = "";
	public string Alertas { get => _alertas; private set => SetProperty(ref _alertas, value); }

	private bool _urgente;
	public bool Urgente { get => _urgente; private set => SetProperty(ref _urgente, value); }

	private bool _temSintomas;
	public bool TemSintomas { get => _temSintomas; private set => SetProperty(ref _temSintomas, value); }

	// Nada conhecido foi citado (e não é urgente).
	private bool _naoReconhecido;
	public bool NaoReconhecido { get => _naoReconhecido; private set => SetProperty(ref _naoReconhecido, value); }

	// A tela mostra o formulário ou o resultado, nunca os dois.
	private bool _mostrandoResultado;
	public bool MostrandoResultado
	{
		get => _mostrandoResultado;
		private set
		{
			if (SetProperty(ref _mostrandoResultado, value))
				Notificar(nameof(Digitando));
		}
	}
	public bool Digitando => !MostrandoResultado;

	private bool _carregando;
	public bool Carregando
	{
		get => _carregando;
		private set
		{
			if (SetProperty(ref _carregando, value))
				AnalisarCommand.AtualizarPodeExecutar();
		}
	}

	private async Task AnalisarAsync()
	{
		TextoErro = ErroGeral = "";
		var texto = Texto.Trim();
		if (texto == "")
		{
			TextoErro = "Descreva os sintomas que você está sentindo.";
			return;
		}
		if (texto.Length > Maximo)
		{
			TextoErro = $"A descrição deve ter no máximo {Maximo} caracteres.";
			return;
		}

		Carregando = true;
		try
		{
			var analise = await _api.PostAsync<AnaliseSintomas>("/api/sintomas/analise", new SintomaRequest(texto));
			Mostrar(analise!);
		}
		catch (ApiException e) when (e.StatusCode != 401)
		{
			if (e.Codigo == "VALIDACAO")
				TextoErro = e.Erros.GetValueOrDefault("texto", e.Message);
			else
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

	private void Mostrar(AnaliseSintomas analise)
	{
		Sintomas.Clear();
		foreach (var sintoma in analise.Sintomas ?? new List<SintomaResultado>())
			Sintomas.Add(new SintomaItem(sintoma));

		Urgente = analise.Urgente;
		Alertas = string.Join("\n", analise.Alertas ?? new List<string>());
		TemSintomas = Sintomas.Count > 0;
		NaoReconhecido = !analise.Reconhecido;
		Aviso = string.IsNullOrWhiteSpace(analise.Aviso) ? AvisoPadrao : analise.Aviso;
		MostrandoResultado = true;
	}

	// Volta ao formulário com o texto anterior, para o usuário ajustar.
	private Task NovaAnalise()
	{
		MostrandoResultado = false;
		return Task.CompletedTask;
	}
}
