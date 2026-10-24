using ApoioSus.Models;
using ApoioSus.Services;
using ApoioSus.Services.Api;

namespace ApoioSus.ViewModels;

// "id" chega pela navegação (medicamento?id=5). Sem id, o formulário cadastra um medicamento novo.
[QueryProperty(nameof(Id), "id")]
public class MedicamentoFormViewModel : ViewModelBase
{
	private const string Rota = "/api/medicamentos";

	private readonly ApiService _api;

	public MedicamentoFormViewModel(ApiService api)
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
	public string Titulo => EhEdicao ? "Editar medicamento" : "Novo medicamento";

	private string _nome = "";
	public string Nome { get => _nome; set => SetProperty(ref _nome, value); }

	private string _dosagem = "";
	public string Dosagem { get => _dosagem; set => SetProperty(ref _dosagem, value); }

	private string _frequencia = "";
	public string Frequencia { get => _frequencia; set => SetProperty(ref _frequencia, value); }

	private string _dataInicio = "";
	public string DataInicio { get => _dataInicio; set => SetProperty(ref _dataInicio, value); }

	private string _dataFim = "";
	public string DataFim { get => _dataFim; set => SetProperty(ref _dataFim, value); }

	private string _nomeErro = "";
	public string NomeErro { get => _nomeErro; set => SetProperty(ref _nomeErro, value); }

	private string _dosagemErro = "";
	public string DosagemErro { get => _dosagemErro; set => SetProperty(ref _dosagemErro, value); }

	private string _frequenciaErro = "";
	public string FrequenciaErro { get => _frequenciaErro; set => SetProperty(ref _frequenciaErro, value); }

	private string _inicioErro = "";
	public string InicioErro { get => _inicioErro; set => SetProperty(ref _inicioErro, value); }

	private string _fimErro = "";
	public string FimErro { get => _fimErro; set => SetProperty(ref _fimErro, value); }

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

	// Chamado quando a tela aparece: na edição, busca os dados do medicamento.
	public async Task CarregarAsync()
	{
		LimparErros();
		if (!EhEdicao)
			return;

		Carregando = true;
		try
		{
			var medicamento = await _api.GetAsync<MedicamentoDados>($"{Rota}/{Id}");
			Nome = medicamento!.Nome;
			Dosagem = medicamento.Dosagem ?? "";
			Frequencia = medicamento.Frequencia ?? "";
			DataInicio = medicamento.DataInicio is null ? "" : Datas.ParaTela(medicamento.DataInicio);
			DataFim = medicamento.DataFim is null ? "" : Datas.ParaTela(medicamento.DataFim);
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
			NomeErro = "Informe o nome do medicamento.";
		else if (Nome.Trim().Length > 150)
			NomeErro = "O nome do medicamento deve ter no máximo 150 caracteres.";

		if (string.IsNullOrWhiteSpace(Dosagem))
			DosagemErro = "Informe a dosagem.";
		else if (Dosagem.Length > 100)
			DosagemErro = "A dosagem deve ter no máximo 100 caracteres.";

		if (string.IsNullOrWhiteSpace(Frequencia))
			FrequenciaErro = "Informe a frequência.";
		else if (Frequencia.Length > 150)
			FrequenciaErro = "A frequência deve ter no máximo 150 caracteres.";

		// A data de início é obrigatória; a data final é opcional (vazio vira null).
		DateOnly? inicio = null;
		if (string.IsNullOrWhiteSpace(DataInicio))
			InicioErro = "Informe a data de início.";
		else if (Datas.TentarLer(DataInicio, out var d))
			inicio = d;
		else
			InicioErro = "Use o formato dd/mm/aaaa.";

		DateOnly? fim = null;
		if (!string.IsNullOrWhiteSpace(DataFim))
		{
			if (Datas.TentarLer(DataFim, out var d))
				fim = d;
			else
				FimErro = "Use o formato dd/mm/aaaa.";
		}

		if (inicio is not null && fim is not null && fim < inicio)
			FimErro = "A data final não pode ser anterior à data de início.";

		if (NomeErro != "" || DosagemErro != "" || FrequenciaErro != "" || InicioErro != "" || FimErro != "")
			return;

		Carregando = true;
		try
		{
			var dados = new MedicamentoRequest(Nome.Trim(), Dosagem.Trim(), Frequencia.Trim(), inicio, fim);
			if (EhEdicao)
				await _api.PutAsync<MedicamentoDados>($"{Rota}/{Id}", dados);
			else
				await _api.PostAsync<MedicamentoDados>(Rota, dados);

			await Shell.Current.GoToAsync("..");
		}
		catch (ApiException e)
		{
			if (e.Codigo == "VALIDACAO")
			{
				NomeErro = e.Erros.GetValueOrDefault("nome", "");
				DosagemErro = e.Erros.GetValueOrDefault("dosagem", "");
				FrequenciaErro = e.Erros.GetValueOrDefault("frequencia", "");
				InicioErro = e.Erros.GetValueOrDefault("dataInicio", "");
				FimErro = e.Erros.GetValueOrDefault("dataFim", "");
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
		bool confirmou = await Shell.Current.DisplayAlertAsync("Excluir medicamento",
			"Deseja excluir este medicamento?", "Excluir", "Cancelar");
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
		NomeErro = DosagemErro = FrequenciaErro = InicioErro = FimErro = ErroGeral = "";
	}
}
