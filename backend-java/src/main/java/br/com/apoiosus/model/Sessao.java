package br.com.apoiosus.model;

import java.time.LocalDateTime;

public record Sessao(int id, int usuarioId, LocalDateTime expiraEm, LocalDateTime revogadaEm) {

    public boolean estaValida() {
        return revogadaEm == null && expiraEm.isAfter(LocalDateTime.now());
    }
}
