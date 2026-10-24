using System.Collections.ObjectModel;
using ApoioSus.Models;
using ApoioSus.Services.Api;

namespace ApoioSus.ViewModels;

// Uma linha da lista de orientações.
public class OrientacaoItem
{
	public string Titulo { get; }
	public string Resumo { get; }
	public string Categoria { get; }
	public string Icone { get; }
	public string TextoBusca { get; }

	public OrientacaoItem(OrientacaoDados orientacao)
	{
		Titulo = orientacao.Titulo;
		Resumo = orientacao.Resumo;
		Categoria = orientacao.Categoria;
		Icone = $"orient_{orientacao.Icone}.png";
		TextoBusca = UnidadeSaudeViewModel.SemAcento($"{Titulo} {Resumo}");
	}
}

// Um botão de filtro (chip) da tela.
public class FiltroOrientacao : ViewModelBase
{
	public string Nome { get; }
	public Comando Comando { get; }

	private bool _selecionado;
	public bool Selecionado { get => _selecionado; set => SetProperty(ref _selecionado, value); }

	public FiltroOrientacao(string nome, Action<FiltroOrientacao> aoTocar)
	{
		Nome = nome;
		Comando = new Comando(() => { aoTocar(this); return Task.CompletedTask; });
	}
}

public class OrientacoesViewModel : ViewModelBase
{
	private const string Todas = "Todas";

	private readonly ApiService _api;
	private List<OrientacaoItem> _todas = new();
	private FiltroOrientacao _filtroAtual;

	public OrientacoesViewModel(ApiService api)
	{
		_api = api;
		VoltarCommand = new Comando(() => Shell.Current.GoToAsync(".."));
		TentarNovamenteCommand = new Comando(CarregarAsync, () => !Carregando);

		Filtros = new List<FiltroOrientacao>();
		foreach (var nome in new[] { Todas, "Prevenção", "Doenças", "Hábitos" })
			Filtros.Add(new FiltroOrientacao(nome, Escolher));
		_filtroAtual = Filtros[0];
		_filtroAtual.Selecionado = true;
	}

	public Comando VoltarCommand { get; }
	public Comando TentarNovamenteCommand { get; }

	public List<FiltroOrientacao> Filtros { get; }
	public ObservableCollection<OrientacaoItem> Orientacoes { get; } = new();

	private string _busca = "";
	public string Busca
	{
		get => _busca;
		set
		{
			if (SetProperty(ref _busca, value ?? ""))
				Filtrar();
		}
	}

	private bool _carregando;
	public bool Carregando
	{
		get => _carregando;
		private set
		{
			if (SetProperty(ref _carregando, value))
			{
				TentarNovamenteCommand.AtualizarPodeExecutar();
				Notificar(nameof(MostrandoLista));
				Notificar(nameof(SemResultado));
			}
		}
	}

	private string _erroGeral = "";
	public string ErroGeral
	{
		get => _erroGeral;
		private set
		{
			if (SetProperty(ref _erroGeral, value))
			{
				Notificar(nameof(TemErro));
				Notificar(nameof(MostrandoLista));
				Notificar(nameof(SemResultado));
			}
		}
	}

	private bool _temOrientacoes;
	public bool TemOrientacoes
	{
		get => _temOrientacoes;
		private set
		{
			if (SetProperty(ref _temOrientacoes, value))
				Notificar(nameof(SemResultado));
		}
	}

	public bool TemErro => ErroGeral != "";
	public bool MostrandoLista => !Carregando && !TemErro;

	// A lista carregou, mas a busca/filtro não achou nada.
	public bool SemResultado => MostrandoLista && !TemOrientacoes;

	// Chamado quando a tela aparece: busca as orientações na API.
	public async Task CarregarAsync()
	{
		ErroGeral = "";
		Carregando = true;
		try
		{
			var resposta = await _api.GetAsync<OrientacoesResposta>("/api/orientacoes");
			_todas = (resposta?.Itens ?? new List<OrientacaoDados>()).Select(o => new OrientacaoItem(o)).ToList();
			Filtrar();
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

	private void Escolher(FiltroOrientacao filtro)
	{
		_filtroAtual.Selecionado = false;
		_filtroAtual = filtro;
		filtro.Selecionado = true;
		Filtrar();
	}

	// Mostra só o que combina com o chip escolhido e com o texto da busca (sem acento, sem diferenciar maiúsculas).
	private void Filtrar()
	{
		var termo = UnidadeSaudeViewModel.SemAcento(Busca.Trim());
		Orientacoes.Clear();
		foreach (var item in _todas)
		{
			bool categoriaOk = _filtroAtual.Nome == Todas || item.Categoria == _filtroAtual.Nome;
			if (categoriaOk && (termo == "" || item.TextoBusca.Contains(termo)))
				Orientacoes.Add(item);
		}
		TemOrientacoes = Orientacoes.Count > 0;
	}
}
