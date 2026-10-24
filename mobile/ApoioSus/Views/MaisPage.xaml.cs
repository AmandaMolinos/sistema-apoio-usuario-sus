using ApoioSus.ViewModels;

namespace ApoioSus.Views;

public partial class MaisPage : ContentPage
{
	public MaisPage(MaisViewModel viewModel)
	{
		InitializeComponent();
		BindingContext = viewModel;
	}
}
