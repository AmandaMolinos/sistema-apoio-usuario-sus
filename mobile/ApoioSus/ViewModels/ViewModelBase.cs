using System.ComponentModel;
using System.Runtime.CompilerServices;

namespace ApoioSus.ViewModels;

// Base de todas as ViewModels. Avisa a tela (View) quando uma propriedade muda,
// para o XAML atualizar o que está ligado (binding) a ela.
public abstract class ViewModelBase : INotifyPropertyChanged
{
	public event PropertyChangedEventHandler? PropertyChanged;

	// Troca o valor do campo e, se mudou, avisa a tela. Retorna true quando houve mudança.
	protected bool SetProperty<T>(ref T campo, T valor, [CallerMemberName] string? nome = null)
	{
		if (EqualityComparer<T>.Default.Equals(campo, valor))
			return false;

		campo = valor;
		Notificar(nome);
		return true;
	}

	// Avisa a tela que uma propriedade mudou (útil para propriedades calculadas).
	protected void Notificar([CallerMemberName] string? nome = null)
		=> PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nome));
}
