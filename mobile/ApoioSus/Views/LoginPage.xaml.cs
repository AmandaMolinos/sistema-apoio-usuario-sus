using ApoioSus.ViewModels;

namespace ApoioSus.Views;

public partial class LoginPage : ContentPage
{
	private readonly LoginViewModel _viewModel;

	public LoginPage(LoginViewModel viewModel)
	{
		InitializeComponent();
		BindingContext = _viewModel = viewModel;
	}

	protected override async void OnAppearing()
	{
		base.OnAppearing();
		_viewModel.LimparCampos();
		await _viewModel.RestaurarSessaoAsync();
		_viewModel.MostrarAviso();
	}
}
