using ApoioSus.ViewModels;

namespace ApoioSus.Views;

public partial class OrientacoesPage : ContentPage
{
	private readonly OrientacoesViewModel _viewModel;

	public OrientacoesPage(OrientacoesViewModel viewModel)
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
