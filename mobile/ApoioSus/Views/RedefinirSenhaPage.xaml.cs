using ApoioSus.ViewModels;

namespace ApoioSus.Views;

public partial class RedefinirSenhaPage : ContentPage
{
	public RedefinirSenhaPage(RedefinirSenhaViewModel viewModel)
	{
		InitializeComponent();
		BindingContext = viewModel;
	}
}
