namespace ApoioSus.Models;

// Corpo de POST /api/sintomas/analise.
public record SintomaRequest(string Texto);

// Orientações para um sintoma reconhecido.
public record SintomaResultado(string Nome, List<string>? Cuidados, List<string>? Procurar);

// Resposta da análise. Urgente = o texto citou um sinal de alerta; Alertas traz as mensagens.
public record AnaliseSintomas(bool Urgente, List<string>? Alertas, List<SintomaResultado>? Sintomas,
	bool Reconhecido, string? Aviso);
