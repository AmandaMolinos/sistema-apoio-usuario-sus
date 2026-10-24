using ApoioSus.ViewModels;

namespace ApoioSus.Views;

public partial class CadastroPage : ContentPage
{
	public CadastroPage(CadastroViewModel viewModel)
	{
		InitializeComponent();
		BindingContext = viewModel;
	}
}
