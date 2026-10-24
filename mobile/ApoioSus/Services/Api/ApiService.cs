using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text.Json;
using ApoioSus.Models;
using ApoioSus.Services.Auth;

namespace ApoioSus.Services.Api;

public class ApiService
{
	private static readonly JsonSerializerOptions Json = new(JsonSerializerDefaults.Web);

	private readonly HttpClient _http;
	private readonly SessaoUsuario _sessao;

	public ApiService(HttpClient http, SessaoUsuario sessao)
	{
		_http = http;
		_sessao = sessao;
	}

	// Windows acessa a API em localhost; o emulador Android enxerga o host em 10.0.2.2.
	public static string BaseUrl =>
		DeviceInfo.Platform == DevicePlatform.Android ? "http://10.0.2.2:8080" : "http://localhost:8080";

	public Task<T?> GetAsync<T>(string rota) => EnviarAsync<T>(HttpMethod.Get, rota, null);

	public Task<T?> PostAsync<T>(string rota, object? corpo) => EnviarAsync<T>(HttpMethod.Post, rota, corpo);

	public Task<T?> PutAsync<T>(string rota, object? corpo) => EnviarAsync<T>(HttpMethod.Put, rota, corpo);

	public async Task DeleteAsync(string rota) => await EnviarAsync<object>(HttpMethod.Delete, rota, null);

	public async Task PostAsync(string rota, object? corpo) => await EnviarAsync<object>(HttpMethod.Post, rota, corpo);

	private async Task<T?> EnviarAsync<T>(HttpMethod metodo, string rota, object? corpo)
	{
		using var requisicao = new HttpRequestMessage(metodo, rota);
		if (_sessao.TemSessao)
			requisicao.Headers.Authorization = new AuthenticationHeaderValue("Bearer", _sessao.Token);
		if (corpo is not null)
			requisicao.Content = JsonContent.Create(corpo, options: Json);

		var enviouToken = _sessao.TemSessao;
		HttpResponseMessage resposta;
		try
		{
			resposta = await _http.SendAsync(requisicao);
		}
		catch (HttpRequestException)
		{
			throw new ApiException(0, "SEM_REDE", "Não foi possível conectar ao servidor. Verifique sua conexão.");
		}
		catch (TaskCanceledException)
		{
			throw new ApiException(0, "TEMPO_ESGOTADO", "O servidor demorou para responder. Tente novamente.");
		}

		using (resposta)
		{
			if (!resposta.IsSuccessStatusCode)
			{
				var erro = await LerErroAsync(resposta);
				// 401 com token enviado: sessão expirada ou revogada. Limpa e leva ao login.
				if (erro.StatusCode == 401 && enviouToken)
					_sessao.Expirar();
				throw erro;
			}

			if (resposta.Content.Headers.ContentLength == 0 || typeof(T) == typeof(object))
				return default;

			return await resposta.Content.ReadFromJsonAsync<T>(Json);
		}
	}

	private static async Task<ApiException> LerErroAsync(HttpResponseMessage resposta)
	{
		var status = (int)resposta.StatusCode;
		try
		{
			var erro = await resposta.Content.ReadFromJsonAsync<ErroResponse>(Json);
			if (erro is not null)
				return new ApiException(status, erro.Codigo, erro.Mensagem, erro.Erros);
		}
		catch (JsonException) { }

		return new ApiException(status, "ERRO_DESCONHECIDO", "Ocorreu um erro inesperado.");
	}
}
