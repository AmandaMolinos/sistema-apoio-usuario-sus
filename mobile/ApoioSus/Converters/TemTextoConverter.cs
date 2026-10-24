using System.Globalization;

namespace ApoioSus.Converters;

// Usado para mostrar mensagens de erro somente quando há texto.
public class TemTextoConverter : IValueConverter
{
	public object Convert(object? value, Type targetType, object? parameter, CultureInfo culture)
		=> !string.IsNullOrEmpty(value as string);

	public object ConvertBack(object? value, Type targetType, object? parameter, CultureInfo culture)
		=> throw new NotSupportedException();
}
