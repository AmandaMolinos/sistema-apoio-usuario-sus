using System.Collections.ObjectModel;
using ApoioSus.Models;
using ApoioSus.Services;
using ApoioSus.Services.Api;

namespace ApoioSus.ViewModels;

// Uma linha da lista de vacinas.
public class VacinaItem
{
	public int Id { get; }
	public string Titulo { get; }
	public string Aplicacao { get; }
	public string Lote { get; }
	public string Local { get; }
	public bool TemLote => Lote != "";
	public bool TemLocal => Local != "";

	public VacinaItem(VacinaDados vacina)
	{
		Id = vacina.Id;
		Titulo = vacina.Nome;
		Aplicacao = "Aplicada em " + Datas.ParaTela(vacina.DataAplicacao);
		Lote = string.IsNullOrWhiteSpace(vacina.Lote) ? "" : "Lote: " + vacina.Lote;
		Local = string.IsNullOrWhiteSpace(vacina.LocalAplicacao) ? "" : vacina.LocalAplicacao!;
	}
}

public class VacinasViewModel : ViewModelBase
{
	private readonly ApiService _api;

	public VacinasViewModel(ApiService api)
	{
		_api = api;
		NovaCommand = new Comando(() => Shell.Current.GoToAsync("vacina"));
	}

	public Comando NovaCommand { get; }

	public ObservableCollection<VacinaItem> Vacinas { get; } = new();

	private bool _temVacinas;
	public bool TemVacinas
	{
		get => _temVacinas;
		set
		{
			if (SetProperty(ref _temVacinas, value))
				Notificar(nameof(ListaVazia));
		}
	}

	public bool ListaVazia => !TemVacinas && !Carregando;

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

	// Tocar em uma vacina da lista abre o formulário de edição.
	public Task AbrirAsync(VacinaItem item) => Shell.Current.GoToAsync($"vacina?id={item.Id}");

	// Chamado quando a tela aparece (inclusive ao voltar do formulário).
	public async Task CarregarAsync()
	{
		ErroGeral = "";
		Carregando = true;
		try
		{
			var vacinas = await _api.GetAsync<List<VacinaDados>>("/api/vacinas");
			Vacinas.Clear();
			foreach (var vacina in vacinas ?? new List<VacinaDados>())
				Vacinas.Add(new VacinaItem(vacina));
			TemVacinas = Vacinas.Count > 0;
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
