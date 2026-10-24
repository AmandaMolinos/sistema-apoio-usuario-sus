using ApoioSus.ViewModels;

namespace ApoioSus.Views;

public partial class VacinaFormPage : ContentPage
{
	private readonly VacinaFormViewModel _viewModel;

	public VacinaFormPage(VacinaFormViewModel viewModel)
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
