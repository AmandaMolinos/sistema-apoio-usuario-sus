using ApoioSus.ViewModels;

namespace ApoioSus.Views;

public partial class SintomasPage : ContentPage
{
	public SintomasPage(SintomasViewModel viewModel)
	{
		InitializeComponent();
		BindingContext = viewModel;
	}
}
