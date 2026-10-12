package br.com.ptcc.sus.interfaces.rest.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public final class Requests {
    private Requests() {
    }

    public record Cadastro(@NotBlank @Size(max = 150) String nome, @NotBlank @Email @Size(max = 255) String email,
                           @NotBlank @Size(min = 8, max = 72) String senha, @Past LocalDate dataNascimento) {
    }

    public record Login(@NotBlank @Email String email, @NotBlank String senha) {
    }

    public record Perfil(@Positive @DecimalMax("1000.0") Double peso, @Positive @DecimalMax("3.0") Double altura,
                         @Pattern(regexp = "^(A|B|AB|O)[+-]?$", message = "Tipo sanguíneo inválido") String tipoSanguineo,
                         @Size(max = 5000) String alergias, @Size(max = 5000) String observacoesMedicas) {
    }

    public record Vacina(@NotBlank @Size(max = 150) String nome,
                         @NotNull @PastOrPresent LocalDate dataAplicacao,
                         @Size(max = 100) String lote,
                         @Size(max = 200) String localAplicacao) {}

    public record Medicamento(@NotBlank @Size(max = 150) String nome,
                              @NotBlank @Size(max = 100) String dosagem,
                              @NotBlank @Size(max = 150) String frequencia,
                              @NotNull LocalDate dataInicio,
                              LocalDate dataFim) {
        @AssertTrue(message = "A data final não pode ser anterior à data inicial")
        public boolean isPeriodoValido() {
            return dataFim == null || dataInicio == null || !dataFim.isBefore(dataInicio);
        }
    }
}