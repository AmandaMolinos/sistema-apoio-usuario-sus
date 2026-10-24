using ApoioSus.ViewModels;

namespace ApoioSus.Views;

public partial class MedicamentosPage : ContentPage
{
	private readonly MedicamentosViewModel _viewModel;

	public MedicamentosPage(MedicamentosViewModel viewModel)
	{
		InitializeComponent();
		BindingContext = _viewModel = viewModel;
	}

	protected override async void OnAppearing()
	{
		base.OnAppearing();
		await _viewModel.CarregarAsync();
	}

	private async void AoTocarMedicamento(object? sender, TappedEventArgs e)
	{
		if ((sender as BindableObject)?.BindingContext is MedicamentoItem item)
			await _viewModel.AbrirAsync(item);
	}
}
