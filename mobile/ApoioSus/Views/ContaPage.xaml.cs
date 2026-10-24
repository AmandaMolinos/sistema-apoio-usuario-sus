using ApoioSus.ViewModels;

namespace ApoioSus.Views;

public partial class ContaPage : ContentPage
{
	private readonly ContaViewModel _viewModel;

	public ContaPage(ContaViewModel viewModel)
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
