using ApoioSus.ViewModels;

namespace ApoioSus.Views;

public partial class UnidadeSaudePage : ContentPage
{
	private readonly UnidadeSaudeViewModel _viewModel;

	public UnidadeSaudePage(UnidadeSaudeViewModel viewModel)
	{
		InitializeComponent();
		BindingContext = _viewModel = viewModel;
	}

	protected override async void OnAppearing()
	{
		base.OnAppearing();
		await _viewModel.CarregarAsync();
	}

	private void AoTocarUnidade(object? sender, TappedEventArgs e)
	{
		if ((sender as BindableObject)?.BindingContext is UnidadeItem item)
			_viewModel.Selecionar(item);
	}
}
