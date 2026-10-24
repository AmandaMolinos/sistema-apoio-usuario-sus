using ApoioSus.Models;
using ApoioSus.Services;
using ApoioSus.Services.Api;

namespace ApoioSus.ViewModels;

// "id" chega pela navegação (vacina?id=5). Sem id, o formulário cadastra uma vacina nova.
[QueryProperty(nameof(Id), "id")]
public class VacinaFormViewModel : ViewModelBase
{
	private const string Rota = "/api/vacinas";

	private readonly ApiService _api;

	public VacinaFormViewModel(ApiService api)
	{
		_api = api;
		SalvarCommand = new Comando(SalvarAsync, () => !Carregando);
		ExcluirCommand = new Comando(ExcluirAsync, () => !Carregando);
		VoltarCommand = new Comando(() => Shell.Current.GoToAsync(".."));
	}

	public Comando SalvarCommand { get; }
	public Comando ExcluirCommand { get; }
	public Comando VoltarCommand { get; }

	private string _id = "";
	public string Id
	{
		get => _id;
		set
		{
			if (SetProperty(ref _id, value ?? ""))
			{
				Notificar(nameof(EhEdicao));
				Notificar(nameof(Titulo));
			}
		}
	}

	public bool EhEdicao => Id != "";
	public string Titulo => EhEdicao ? "Editar vacina" : "Nova vacina";

	private string _nome = "";
	public string Nome { get => _nome; set => SetProperty(ref _nome, value); }

	private string _dataAplicacao = "";
	public string DataAplicacao { get => _dataAplicacao; set => SetProperty(ref _dataAplicacao, value); }

	private string _lote = "";
	public string Lote { get => _lote; set => SetProperty(ref _lote, value); }

	private string _local = "";
	public string Local { get => _local; set => SetProperty(ref _local, value); }

	private string _nomeErro = "";
	public string NomeErro { get => _nomeErro; set => SetProperty(ref _nomeErro, value); }

	private string _dataErro = "";
	public string DataErro { get => _dataErro; set => SetProperty(ref _dataErro, value); }

	private string _loteErro = "";
	public string LoteErro { get => _loteErro; set => SetProperty(ref _loteErro, value); }

	private string _localErro = "";
	public string LocalErro { get => _localErro; set => SetProperty(ref _localErro, value); }

	private string _erroGeral = "";
	public string ErroGeral { get => _erroGeral; set => SetProperty(ref _erroGeral, value); }

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

	// Chamado quando a tela aparece: na edição, busca os dados da vacina.
	public async Task CarregarAsync()
	{
		LimparErros();
		if (!EhEdicao)
			return;

		Carregando = true;
		try
		{
			var vacina = await _api.GetAsync<VacinaDados>($"{Rota}/{Id}");
			Nome = vacina!.Nome;
			DataAplicacao = Datas.ParaTela(vacina.DataAplicacao);
			Lote = vacina.Lote ?? "";
			Local = vacina.LocalAplicacao ?? "";
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
		LimparErros();

		if (string.IsNullOrWhiteSpace(Nome))
			NomeErro = "Informe o nome da vacina.";
		else if (Nome.Trim().Length > 150)
			NomeErro = "O nome da vacina deve ter no máximo 150 caracteres.";

		DateOnly data = default;
		if (string.IsNullOrWhiteSpace(DataAplicacao))
			DataErro = "Informe a data de aplicação.";
		else if (!Datas.TentarLer(DataAplicacao, out data))
			DataErro = "Use o formato dd/mm/aaaa.";
		else if (Datas.EhFutura(data))
			DataErro = "A data de aplicação não pode ser futura.";

		if (Lote.Length > 100)
			LoteErro = "O lote deve ter no máximo 100 caracteres.";
		if (Local.Length > 200)
			LocalErro = "O local deve ter no máximo 200 caracteres.";

		if (NomeErro != "" || DataErro != "" || LoteErro != "" || LocalErro != "")
			return;

		Carregando = true;
		try
		{
			var dados = new VacinaRequest(Nome.Trim(), data, Lote, Local);
			if (EhEdicao)
				await _api.PutAsync<VacinaDados>($"{Rota}/{Id}", dados);
			else
				await _api.PostAsync<VacinaDados>(Rota, dados);

			await Shell.Current.GoToAsync("..");
		}
		catch (ApiException e)
		{
			if (e.Codigo == "VALIDACAO")
			{
				NomeErro = e.Erros.GetValueOrDefault("nome", "");
				DataErro = e.Erros.GetValueOrDefault("dataAplicacao", "");
				LoteErro = e.Erros.GetValueOrDefault("lote", "");
				LocalErro = e.Erros.GetValueOrDefault("localAplicacao", "");
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
		bool confirmou = await Shell.Current.DisplayAlertAsync("Excluir vacina",
			"Deseja excluir este registro de vacina?", "Excluir", "Cancelar");
		if (!confirmou)
			return;

		Carregando = true;
		try
		{
			await _api.DeleteAsync($"{Rota}/{Id}");
			await Shell.Current.GoToAsync("..");
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

	private void LimparErros()
	{
		NomeErro = DataErro = LoteErro = LocalErro = ErroGeral = "";
	}
}
