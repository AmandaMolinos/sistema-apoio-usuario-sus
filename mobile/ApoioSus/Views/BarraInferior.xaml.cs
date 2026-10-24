namespace ApoioSus.Views;

// Barra de abas das telas principais. Use assim: <views:BarraInferior Atual="vacinas" />
// Valores de Atual: inicio, vacinas, medicamentos, unidade-saude, mais.
public partial class BarraInferior : ContentView
{
	public static readonly BindableProperty AtualProperty = BindableProperty.Create(
		nameof(Atual), typeof(string), typeof(BarraInferior), "",
		propertyChanged: (bindable, _, _) => ((BarraInferior)bindable).Destacar());

	public BarraInferior()
	{
		InitializeComponent();
		Destacar();
	}

	public string Atual
	{
		get => (string)GetValue(AtualProperty);
		set => SetValue(AtualProperty, value);
	}

	// O item atual fica azul; os outros, cinza.
	private void Destacar()
	{
		Pintar(IconeInicio, TextoInicio, "inicio", "inicio");
		Pintar(IconeVacinas, TextoVacinas, "vacinas", "vacinas");
		Pintar(IconeMedicamentos, TextoMedicamentos, "medicamentos", "medicamentos");
		Pintar(IconeUnidades, TextoUnidades, "unidade-saude", "unidades");
		Pintar(IconeMais, TextoMais, "mais", "mais");
	}

	private void Pintar(Image icone, Label texto, string rota, string arquivo)
	{
		bool atual = Atual == rota;
		icone.Source = atual ? $"bar_{arquivo}_azul.png" : $"bar_{arquivo}_cinza.png";
		texto.TextColor = Cor(atual ? "Primary" : "CinzaIcone");
		texto.FontAttributes = atual ? FontAttributes.Bold : FontAttributes.None;
	}

	private static Color Cor(string chave)
		=> Application.Current?.Resources.TryGetValue(chave, out var valor) == true && valor is Color cor
			? cor
			: Colors.Gray;

	private async void AoTocar(object? sender, TappedEventArgs e)
	{
		var rota = e.Parameter as string;
		if (string.IsNullOrEmpty(rota) || rota == Atual)
			return;

		try
		{
			await Shell.Current.GoToAsync($"//{rota}");
		}
		catch (Exception ex)
		{
			System.Diagnostics.Debug.WriteLine(ex);
		}
	}
}
