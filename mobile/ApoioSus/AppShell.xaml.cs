namespace ApoioSus;

public partial class AppShell : Shell
{
	public AppShell()
	{
		InitializeComponent();
		Routing.RegisterRoute("cadastro", typeof(Views.CadastroPage));
		Routing.RegisterRoute("recuperar-senha", typeof(Views.RecuperarSenhaPage));
		Routing.RegisterRoute("redefinir-senha", typeof(Views.RedefinirSenhaPage));
		Routing.RegisterRoute("conta", typeof(Views.ContaPage));
		Routing.RegisterRoute("perfil-saude", typeof(Views.PerfilSaudePage));
		Routing.RegisterRoute("orientacoes", typeof(Views.OrientacoesPage));
		Routing.RegisterRoute("sintomas", typeof(Views.SintomasPage));
		Routing.RegisterRoute("documento", typeof(Views.DocumentoPage));
		Routing.RegisterRoute("vacina",typeof(Views.VacinaFormPage));
		Routing.RegisterRoute("medicamento", typeof(Views.MedicamentoFormPage));

		var sessao = IPlatformApplication.Current?.Services.GetService<Services.Auth.SessaoUsuario>();
		if (sessao is not null)
			sessao.Expirada += () => MainThread.BeginInvokeOnMainThread(async () =>
			{
				if (!CurrentState.Location.OriginalString.Contains("login"))
					await GoToAsync("//login");
			});
	}
}
