package br.com.apoiosus.config;

// Identidade colocada no contexto de segurança após validar o token da requisição.
public record UsuarioAutenticado(Integer usuarioId, Integer sessaoId) {
}
