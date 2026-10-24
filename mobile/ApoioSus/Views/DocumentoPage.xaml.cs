using ApoioSus.ViewModels;

namespace ApoioSus.Views;

public partial class DocumentoPage : ContentPage
{
	public DocumentoPage(DocumentoViewModel viewModel)
	{
		InitializeComponent();
		BindingContext = viewModel;
	}
}
