using ApoioSus.ViewModels;

namespace ApoioSus.Views;

public partial class RecuperarSenhaPage : ContentPage
{
	public RecuperarSenhaPage(RecuperarSenhaViewModel viewModel)
	{
		InitializeComponent();
		BindingContext = viewModel;
	}
}
