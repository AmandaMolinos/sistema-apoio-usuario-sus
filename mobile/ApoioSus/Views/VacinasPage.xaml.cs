using ApoioSus.ViewModels;

namespace ApoioSus.Views;

public partial class VacinasPage : ContentPage
{
	private readonly VacinasViewModel _viewModel;

	public VacinasPage(VacinasViewModel viewModel)
	{
		InitializeComponent();
		BindingContext = _viewModel = viewModel;
	}

	protected override async void OnAppearing()
	{
		base.OnAppearing();
		await _viewModel.CarregarAsync();
	}

	private async void AoTocarVacina(object? sender, TappedEventArgs e)
	{
		if ((sender as BindableObject)?.BindingContext is VacinaItem item)
			await _viewModel.AbrirAsync(item);
	}
}
