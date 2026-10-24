using ApoioSus.ViewModels;

namespace ApoioSus.Views;

public partial class PerfilSaudePage : ContentPage
{
	private readonly PerfilSaudeViewModel _viewModel;

	public PerfilSaudePage(PerfilSaudeViewModel viewModel)
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
