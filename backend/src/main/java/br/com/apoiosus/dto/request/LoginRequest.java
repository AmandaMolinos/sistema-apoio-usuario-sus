package br.com.apoiosus.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Informe o e-mail.")
        String email,

        @NotBlank(message = "Informe a senha.")
        String senha) {
}
