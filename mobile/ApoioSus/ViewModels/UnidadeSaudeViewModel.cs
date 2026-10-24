using System.Collections.ObjectModel;
using System.Globalization;
using System.Text;
using ApoioSus.Models;
using ApoioSus.Services;
using ApoioSus.Services.Api;

namespace ApoioSus.ViewModels;

// Uma linha da lista de UPAs.
public class UnidadeItem
{
	public string Titulo { get; }
	public string Endereco { get; }
	public string Contato { get; }
	public string Atualizacao { get; }
	public string Rua { get; }
	public string Bairro { get; }
	public string Telefone { get; }
	public bool TemTelefone => Telefone != "";
	public string ConsultaRota { get; }
	public string TextoBusca { get; }

	public UnidadeItem(UnidadeDados unidade)
	{
		Titulo = string.IsNullOrWhiteSpace(unidade.Nome) ? "Nome não informado" : unidade.Nome;

		var partes = new List<string>();
		if (!string.IsNullOrWhiteSpace(unidade.Logradouro))
			partes.Add(string.IsNullOrWhiteSpace(unidade.Numero)
				? unidade.Logradouro
				: $"{unidade.Logradouro}, {unidade.Numero}");
		if (!string.IsNullOrWhiteSpace(unidade.Bairro))
			partes.Add(unidade.Bairro);
		if (!string.IsNullOrWhiteSpace(unidade.Cep))
			partes.Add("CEP " + FormatarCep(unidade.Cep));
		Endereco = partes.Count > 0 ? string.Join(" · ", partes) : "Endereço não informado";

		var contato = new List<string>();
		if (unidade.Telefones is not null)
			contato.AddRange(unidade.Telefones);
		if (!string.IsNullOrWhiteSpace(unidade.Email))
			contato.Add(unidade.Email);
		Contato = contato.Count > 0 ? string.Join(" · ", contato) : "Contato não informado";

		Rua = string.IsNullOrWhiteSpace(unidade.Logradouro)
			? "Endereço não informado"
			: string.IsNullOrWhiteSpace(unidade.Numero) ? unidade.Logradouro : $"{unidade.Logradouro}, {unidade.Numero}";
		Bairro = unidade.Bairro?.Trim() ?? "";
		Telefone = unidade.Telefones?.FirstOrDefault(t => !string.IsNullOrWhiteSpace(t)) ?? "";
		ConsultaRota = string.Join(", ", new[] { Titulo, unidade.Logradouro, unidade.Numero, unidade.Bairro, unidade.Municipio, unidade.Uf ?? "SP" }
			.Where(t => !string.IsNullOrWhiteSpace(t)));
		TextoBusca = UnidadeSaudeViewModel.SemAcento($"{Titulo} {Rua} {Bairro}");

		Atualizacao = unidade.AtualizadoEm is null
			? "Data de atualização não informada"
			: "Atualizado em " + Datas.ParaTela(unidade.AtualizadoEm);
	}

	private static string FormatarCep(string cep)
		=> cep.Length == 8 && cep.All(char.IsDigit) ? cep[..5] + "-" + cep[5..] : cep;
}

public class UnidadeSaudeViewModel : ViewModelBase
{
	private const string Rota = "/api/unidades-saude";

	private readonly ApiService _api;
	private readonly List<MunicipioDados> _todosMunicipios = new();
	private int? _proximo;
	private Func<Task> _tentarNovamente;

	public UnidadeSaudeViewModel(ApiService api)
	{
		_api = api;
		_tentarNovamente = CarregarMunicipiosAsync;
		TrocarMunicipioCommand = new Comando(TrocarMunicipio);
		CarregarMaisCommand = new Comando(CarregarMaisAsync, () => !Carregando);
		TentarNovamenteCommand = new Comando(() => _tentarNovamente(), () => !Carregando);
		VerMapaCommand = new Comando(() => { ModoMapa = true; return Task.CompletedTask; });
		VerListaCommand = new Comando(() => { ModoMapa = false; return Task.CompletedTask; });
		VerRotasCommand = new Comando(VerRotasAsync);
	}

	public Comando TrocarMunicipioCommand { get; }
	public Comando CarregarMaisCommand { get; }
	public Comando TentarNovamenteCommand { get; }
	public Comando VerMapaCommand { get; }
	public Comando VerListaCommand { get; }
	public Comando VerRotasCommand { get; }

	public ObservableCollection<MunicipioDados> Municipios { get; } = new();
	// Unidades mostradas (já filtradas pela busca); _todasUnidades guarda tudo o que foi carregado.
	public ObservableCollection<UnidadeItem> Unidades { get; } = new();
	private readonly List<UnidadeItem> _todasUnidades = new();

	// Abas Mapa / Lista
	private bool _modoMapa = true;
	public bool ModoMapa
	{
		get => _modoMapa;
		set
		{
			if (SetProperty(ref _modoMapa, value))
				Notificar(nameof(ModoLista));
		}
	}
	public bool ModoLista => !ModoMapa;

	// Busca por nome ou endereço entre as unidades já carregadas.
	private string _buscaUnidade = "";
	public string BuscaUnidade
	{
		get => _buscaUnidade;
		set
		{
			if (SetProperty(ref _buscaUnidade, value ?? ""))
				FiltrarUnidades();
		}
	}

	// Unidade cujo cartão aparece embaixo do mapa.
	private UnidadeItem? _selecionada;
	public UnidadeItem? Selecionada
	{
		get => _selecionada;
		private set
		{
			if (SetProperty(ref _selecionada, value))
				Notificar(nameof(TemSelecionada));
		}
	}
	public bool TemSelecionada => Selecionada is not null;

	// Tocar em um cartão da lista mostra a unidade no modo mapa.
	public void Selecionar(UnidadeItem item)
	{
		Selecionada = item;
		ModoMapa = true;
	}

	// Abre o endereço da unidade no mapa do navegador/aplicativo padrão.
	private async Task VerRotasAsync()
	{
		if (Selecionada is null)
			return;
		try
		{
			var url = "https://www.google.com/maps/search/?api=1&query=" + Uri.EscapeDataString(Selecionada.ConsultaRota);
			await Launcher.Default.OpenAsync(new Uri(url));
		}
		catch (Exception e)
		{
			ErroGeral = "Não foi possível abrir o mapa: " + e.Message;
		}
	}

	private void FiltrarUnidades()
	{
		var termo = SemAcento(BuscaUnidade.Trim());
		Unidades.Clear();
		foreach (var item in _todasUnidades)
		{
			if (termo == "" || item.TextoBusca.Contains(termo))
				Unidades.Add(item);
		}
		if (Selecionada is null || !Unidades.Contains(Selecionada))
			Selecionada = Unidades.FirstOrDefault();
		AtualizarEstadoDaLista();
	}

	// Texto digitado para achar o município; filtra a lista sem acento e sem diferenciar maiúsculas.
	private string _busca = "";
	public string Busca
	{
		get => _busca;
		set
		{
			if (SetProperty(ref _busca, value ?? ""))
				FiltrarMunicipios();
		}
	}

	// Tocar em um município da lista busca as UPAs dele.
	private MunicipioDados? _municipioTocado;
	public MunicipioDados? MunicipioTocado
	{
		get => _municipioTocado;
		set
		{
			if (SetProperty(ref _municipioTocado, value) && value is not null)
				_ = EscolherAsync(value);
		}
	}

	private MunicipioDados? _municipio;
	public MunicipioDados? Municipio
	{
		get => _municipio;
		private set
		{
			if (SetProperty(ref _municipio, value))
			{
				Notificar(nameof(EscolhendoMunicipio));
				Notificar(nameof(MostrandoUnidades));
				Notificar(nameof(TituloResultado));
				AtualizarEstadoDaLista();
			}
		}
	}

	public bool EscolhendoMunicipio => Municipio is null;
	public bool MostrandoUnidades => Municipio is not null;
	public string TituloResultado => Municipio is null ? "" : $"UPAs do SUS em {Municipio.Nome}";

	private bool _buscou;
	private bool _temMais;
	public bool TemMais
	{
		get => _temMais;
		private set => SetProperty(ref _temMais, value);
	}

	// A busca terminou sem erro e não achou nenhuma UPA do SUS.
	public bool SemUnidades => Municipio is not null && _buscou && _todasUnidades.Count == 0 && !Carregando
		&& ErroGeral == "";

	public bool TemUnidades => Unidades.Count > 0;

	private bool _carregando;
	public bool Carregando
	{
		get => _carregando;
		private set
		{
			if (SetProperty(ref _carregando, value))
			{
				CarregarMaisCommand.AtualizarPodeExecutar();
				TentarNovamenteCommand.AtualizarPodeExecutar();
				Notificar(nameof(SemUnidades));
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
				Notificar(nameof(SemUnidades));
		}
	}

	// Chamado quando a tela aparece. Só busca a lista de municípios na primeira vez.
	public async Task CarregarAsync()
	{
		if (_todosMunicipios.Count == 0)
			await CarregarMunicipiosAsync();
	}

	private async Task CarregarMunicipiosAsync()
	{
		_tentarNovamente = CarregarMunicipiosAsync;
		ErroGeral = "";
		Carregando = true;
		try
		{
			var municipios = await _api.GetAsync<List<MunicipioDados>>($"{Rota}/municipios");
			_todosMunicipios.Clear();
			_todosMunicipios.AddRange(municipios ?? new List<MunicipioDados>());
			FiltrarMunicipios();
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

	private void FiltrarMunicipios()
	{
		var termo = SemAcento(Busca.Trim());
		Municipios.Clear();
		foreach (var municipio in _todosMunicipios)
		{
			if (termo == "" || SemAcento(municipio.Nome).Contains(termo))
				Municipios.Add(municipio);
		}
	}

	private async Task EscolherAsync(MunicipioDados municipio)
	{
		try
		{
			Municipio = municipio;
			await BuscarAsync(1, limpar: true);
		}
		finally
		{
			MunicipioTocado = null; // permite tocar no mesmo município de novo
		}
	}

	private Task TrocarMunicipio()
	{
		Municipio = null;
		_todasUnidades.Clear();
		BuscaUnidade = "";
		FiltrarUnidades();
		_proximo = null;
		_buscou = false;
		TemMais = false;
		ErroGeral = "";
		_tentarNovamente = CarregarMunicipiosAsync;
		AtualizarEstadoDaLista();
		return Task.CompletedTask;
	}

	private Task CarregarMaisAsync() => _proximo is null ? Task.CompletedTask : BuscarAsync(_proximo.Value, limpar: false);

	private async Task BuscarAsync(int inicio, bool limpar)
	{
		var municipio = Municipio;
		if (municipio is null)
			return;

		_tentarNovamente = () => BuscarAsync(inicio, limpar);
		ErroGeral = "";
		if (limpar)
		{
			_todasUnidades.Clear();
			FiltrarUnidades();
			_proximo = null;
			_buscou = false;
			TemMais = false;
		}

		Carregando = true;
		try
		{
			var pagina = await _api.GetAsync<PaginaUnidades>($"{Rota}/upas?municipio={municipio.Codigo}&inicio={inicio}");
			// O usuário pode ter trocado de município enquanto a resposta chegava.
			if (Municipio != municipio)
				return;

			foreach (var unidade in pagina?.Itens ?? new List<UnidadeDados>())
				_todasUnidades.Add(new UnidadeItem(unidade));
			FiltrarUnidades();
			_proximo = pagina?.Proximo;
			TemMais = _proximo is not null;
			_buscou = true;
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
			AtualizarEstadoDaLista();
		}
	}

	private void AtualizarEstadoDaLista()
	{
		Notificar(nameof(SemUnidades));
		Notificar(nameof(TemUnidades));
	}

	public static string SemAcento(string texto)
	{
		var decomposto = texto.Normalize(NormalizationForm.FormD);
		var sb = new StringBuilder(decomposto.Length);
		foreach (var c in decomposto)
		{
			if (CharUnicodeInfo.GetUnicodeCategory(c) != UnicodeCategory.NonSpacingMark)
				sb.Append(c);
		}
		return sb.ToString().ToLowerInvariant();
	}
}
