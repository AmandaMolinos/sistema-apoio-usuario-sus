package br.com.apoiosus.dto.request;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

import br.com.apoiosus.exception.ApiException;

public record CadastroRequest(String nome, String email, String senha, String dataNascimento) {

    public void validar() {
        Map<String, String> erros = new LinkedHashMap<>();

        if (nome == null || nome.isBlank()) {
            erros.put("nome", "Informe o nome.");
        } else if (nome.length() > 150) {
            erros.put("nome", "O nome deve ter no máximo 150 caracteres.");
        }

        if (email == null || email.isBlank()) {
            erros.put("email", "Informe o e-mail.");
        } else if (!email.trim().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            erros.put("email", "E-mail inválido.");
        } else if (email.length() > 255) {
            erros.put("email", "O e-mail deve ter no máximo 255 caracteres.");
        }

        if (senha == null || senha.isBlank()) {
            erros.put("senha", "Informe a senha.");
        } else if (senha.length() < 8 || senha.length() > 72) {
            erros.put("senha", "A senha deve ter entre 8 e 72 caracteres.");
        }

        // A data é opcional; se vier preenchida, precisa ser aaaa-mm-dd e não pode ser futura.
        if (dataNascimento != null && !dataNascimento.isBlank()) {
            try {
                if (LocalDate.parse(dataNascimento.trim()).isAfter(LocalDate.now())) {
                    erros.put("dataNascimento", "A data de nascimento não pode ser futura.");
                }
            } catch (DateTimeParseException e) {
                erros.put("dataNascimento", "Data inválida. Use o formato aaaa-mm-dd.");
            }
        }

        if (!erros.isEmpty()) {
            throw ApiException.validacao(erros);
        }
    }

    // Data pronta para salvar: texto aaaa-mm-dd ou null quando não foi informada.
    public String dataNascimentoOuNull() {
        return dataNascimento == null || dataNascimento.isBlank() ? null : dataNascimento.trim();
    }
}
