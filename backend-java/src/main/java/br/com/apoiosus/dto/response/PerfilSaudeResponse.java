package br.com.apoiosus.dto.response;

public record PerfilSaudeResponse(int id, int usuarioId, Double peso, Double altura, String tipoSanguineo,
        String alergias, String observacoesMedicas) {
}
