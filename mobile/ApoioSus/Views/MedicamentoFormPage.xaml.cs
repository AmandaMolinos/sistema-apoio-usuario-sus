using ApoioSus.ViewModels;

namespace ApoioSus.Views;

public partial class MedicamentoFormPage : ContentPage
{
	private readonly MedicamentoFormViewModel _viewModel;

	public MedicamentoFormPage(MedicamentoFormViewModel viewModel)
	{
		InitializeComponent();
		BindingContext = _viewModel = viewModel;
	}

	protected override async void OnAppearing()
	{
		base.OnAppearing();
		await _viewModel.CarregarAsync();
	}
}
