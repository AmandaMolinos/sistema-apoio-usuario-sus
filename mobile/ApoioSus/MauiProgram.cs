using ApoioSus.Services.Api;
using ApoioSus.Services.Auth;
using ApoioSus.ViewModels;
using ApoioSus.Views;
using Microsoft.Extensions.Logging;

namespace ApoioSus;

public static class MauiProgram
{
	public static MauiApp CreateMauiApp()
	{
		var builder = MauiApp.CreateBuilder();
		builder
			.UseMauiApp<App>()
			.ConfigureFonts(fonts => fonts.AddFont("OpenSans-Regular.ttf", "OpenSansRegular"));

		builder.Services.AddSingleton(new HttpClient { BaseAddress = new Uri(ApiService.BaseUrl), Timeout = TimeSpan.FromSeconds(60) });
		builder.Services.AddSingleton<SessaoUsuario>();
		builder.Services.AddSingleton<ApiService>();
		builder.Services.AddTransient<LoginViewModel>();
		builder.Services.AddTransient<LoginPage>();
		builder.Services.AddTransient<InicioViewModel>();
		builder.Services.AddTransient<InicioPage>();
		builder.Services.AddTransient<CadastroViewModel>();
		builder.Services.AddTransient<CadastroPage>();
		builder.Services.AddTransient<RecuperarSenhaViewModel>();
		builder.Services.AddTransient<RecuperarSenhaPage>();
		builder.Services.AddTransient<RedefinirSenhaViewModel>();
		builder.Services.AddTransient<RedefinirSenhaPage>();
		builder.Services.AddTransient<ContaViewModel>();
		builder.Services.AddTransient<ContaPage>();
		builder.Services.AddTransient<PerfilSaudeViewModel>();
		builder.Services.AddTransient<PerfilSaudePage>();
		builder.Services.AddTransient<VacinasViewModel>();
		builder.Services.AddTransient<VacinasPage>();
		builder.Services.AddTransient<VacinaFormViewModel>();
		builder.Services.AddTransient<VacinaFormPage>();
		builder.Services.AddTransient<MedicamentosViewModel>();
		builder.Services.AddTransient<MedicamentosPage>();
		builder.Services.AddTransient<MedicamentoFormViewModel>();
		builder.Services.AddTransient<MedicamentoFormPage>();
		builder.Services.AddTransient<DocumentoViewModel>();
		builder.Services.AddTransient<DocumentoPage>();
		builder.Services.AddTransient<SintomasViewModel>();
		builder.Services.AddTransient<SintomasPage>();
		builder.Services.AddTransient<OrientacoesViewModel>();
		builder.Services.AddTransient<OrientacoesPage>();
		builder.Services.AddTransient<UnidadeSaudeViewModel>();
		builder.Services.AddTransient<UnidadeSaudePage>();
		builder.Services.AddTransient<MaisViewModel>();
		builder.Services.AddTransient<MaisPage>();

#if DEBUG
		builder.Logging.AddDebug();
#endif

		return builder.Build();
	}
}
