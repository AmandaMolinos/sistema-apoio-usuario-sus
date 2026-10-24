using System.Windows.Input;

namespace ApoioSus.ViewModels;

// Um comando é a ação ligada a um botão (Command="{Binding ...}").
// Pode ter uma condição (podeExecutar): quando é falsa, o botão fica desabilitado.
public class Comando : ICommand
{
	private readonly Func<Task> _executar;
	private readonly Func<bool>? _podeExecutar;

	public Comando(Func<Task> executar, Func<bool>? podeExecutar = null)
	{
		_executar = executar;
		_podeExecutar = podeExecutar;
	}

	public event EventHandler? CanExecuteChanged;

	public bool CanExecute(object? parametro) => _podeExecutar?.Invoke() ?? true;

	public async void Execute(object? parametro)
	{
		try
		{
			await _executar();
		}
		catch (Exception e)
		{
			// Erros esperados da API são tratados nas ViewModels; aqui só evitamos fechar o app.
			System.Diagnostics.Debug.WriteLine(e);
			if (Shell.Current is not null)
				await Shell.Current.DisplayAlertAsync("Erro", "Ocorreu um erro inesperado.", "OK");
		}
	}

	// Chame quando a condição do botão pode ter mudado (ex.: começou ou terminou de carregar).
	public void AtualizarPodeExecutar() => CanExecuteChanged?.Invoke(this, EventArgs.Empty);
}
