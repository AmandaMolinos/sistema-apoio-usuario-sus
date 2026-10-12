package br.com.ptcc.sus.interfaces.rest.dto;

import br.com.ptcc.sus.domain.model.*;

import java.time.LocalDate;

public final class Responses {
    private Responses() {
    }

    public record Usuario(Long id, String nome, String email, java.time.LocalDate dataNascimento) {
        public static Usuario of(br.com.ptcc.sus.domain.model.Usuario u) {
            return new Usuario(u.id(), u.nome(), u.email(), u.dataNascimento());
        }
    }

    public record Auth(String token, String tipo, long expiresIn) {
    }

    public record Perfil(Long id, Long usuarioId, Double peso, Double altura, String tipoSanguineo, String alergias,
                         String observacoesMedicas) {
        public static Perfil of(PerfilSaude p) {
            return new Perfil(p.id(), p.usuarioId(), p.peso(), p.altura(), p.tipoSanguineo(), p.alergias(), p.observacoesMedicas());
        }
    }

    public record Vacina(Long id, String nome, LocalDate dataAplicacao, String lote, String localAplicacao) {
        public static Vacina of(br.com.ptcc.sus.domain.model.Vacina v) {
            return new Vacina(v.id(), v.nome(), v.dataAplicacao(), v.lote(), v.localAplicacao());
        }
    }
    public record Medicamento(Long id, String nome, String dosagem, String frequencia,
                              LocalDate dataInicio, LocalDate dataFim) {
        public static Medicamento of(br.com.ptcc.sus.domain.model.Medicamento m) {
            return new Medicamento(m.id(), m.nome(), m.dosagem(), m.frequencia(), m.dataInicio(), m.dataFim());
        }
    }
}