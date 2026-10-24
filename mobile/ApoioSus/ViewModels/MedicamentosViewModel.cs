using System.Collections.ObjectModel;
using ApoioSus.Models;
using ApoioSus.Services;
using ApoioSus.Services.Api;

namespace ApoioSus.ViewModels;

// Uma linha da lista de medicamentos.
public class MedicamentoItem
{
	private static readonly string[] Icones =
		{ "medicamento_vermelho.png", "medicamento_azul.png", "medicamento_amarelo.png" };

	public int Id { get; }
	public string Titulo { get; }
	public string Dosagem { get; }
	public string Frequencia { get; }
	public string Inicio { get; }
	public string Fim { get; }
	public string Icone { get; }

	// Em uso: sem data final, ou com data final de hoje em diante.
	public bool EmUso { get; }

	public bool TemDosagem => Dosagem != "";
	public bool TemFrequencia => Frequencia != "";
	public bool TemInicio => Inicio != "";
	public bool TemFim => Fim != "";

	public MedicamentoItem(MedicamentoDados medicamento)
	{
		Id = medicamento.Id;
		Titulo = medicamento.Nome;
		Dosagem = medicamento.Dosagem?.Trim() ?? "";
		Frequencia = medicamento.Frequencia?.Trim() ?? "";
		Inicio = medicamento.DataInicio is null ? "" : "Início: " + Datas.ParaTela(medicamento.DataInicio);
		Fim = medicamento.DataFim is null ? "" : "Fim: " + Datas.ParaTela(medicamento.DataFim);
		Icone = Icones[medicamento.Id % Icones.Length];
		EmUso = medicamento.DataFim is null || medicamento.DataFim >= DateOnly.FromDateTime(DateTime.Today);
	}
}

public class MedicamentosViewModel : ViewModelBase
{
	private readonly ApiService _api;
	private List<MedicamentoItem> _todos = new();

	public MedicamentosViewModel(ApiService api)
	{
		_api = api;
		NovoCommand = new Comando(() => Shell.Current.GoToAsync("medicamento"));
		VerEmUsoCommand = new Comando(() => { EmUso = true; return Task.CompletedTask; });
		VerHistoricoCommand = new Comando(() => { EmUso = false; return Task.CompletedTask; });
	}

	public Comando NovoCommand { get; }
	public Comando VerEmUsoCommand { get; }
	public Comando VerHistoricoCommand { get; }

	// Lista da aba atual
	public ObservableCollection<MedicamentoItem> Medicamentos { get; } = new();

	// Abas "Em uso" e "Histórico"
	private bool _emUso = true;
	public bool EmUso
	{
		get => _emUso;
		set
		{
			if (SetProperty(ref _emUso, value))
			{
				Notificar(nameof(Historico));
				Filtrar();
			}
		}
	}

	public bool Historico => !EmUso;

	private bool _temMedicamentos;
	public bool TemMedicamentos
	{
		get => _temMedicamentos;
		set
		{
			if (SetProperty(ref _temMedicamentos, value))
				Notificar(nameof(ListaVazia));
		}
	}

	public bool ListaVazia => !TemMedicamentos && !Carregando;

	private bool _carregando;
	public bool Carregando
	{
		get => _carregando;
		set
		{
			if (SetProperty(ref _carregando, value))
				Notificar(nameof(ListaVazia));
		}
	}

	private string _erroGeral = "";
	public string ErroGeral { get => _erroGeral; set => SetProperty(ref _erroGeral, value); }

	// Tocar em um medicamento da lista abre o formulário de edição.
	public Task AbrirAsync(MedicamentoItem item) => Shell.Current.GoToAsync($"medicamento?id={item.Id}");

	// Mostra na lista só os medicamentos da aba atual.
	private void Filtrar()
	{
		Medicamentos.Clear();
		foreach (var item in _todos.Where(m => m.EmUso == EmUso))
			Medicamentos.Add(item);
		TemMedicamentos = Medicamentos.Count > 0;
	}

	// Chamado quando a tela aparece (inclusive ao voltar do formulário).
	public async Task CarregarAsync()
	{
		ErroGeral = "";
		Carregando = true;
		try
		{
			var medicamentos = await _api.GetAsync<List<MedicamentoDados>>("/api/medicamentos");
			_todos = (medicamentos ?? new List<MedicamentoDados>()).Select(m => new MedicamentoItem(m)).ToList();
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
}
