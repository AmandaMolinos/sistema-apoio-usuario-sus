package br.com.apoiosus.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import br.com.apoiosus.Banco;
import br.com.apoiosus.model.Sessao;

public class SessaoRepository {

    private final Banco banco;

    public SessaoRepository(Banco banco) {
        this.banco = banco;
    }

    public void inserir(int usuarioId, String tokenHash, LocalDateTime criadoEm, LocalDateTime expiraEm)
            throws SQLException {
        try (Connection c = banco.conectar();
                PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO sessao (id_usuario, token_hash, criado_em, expira_em) VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, usuarioId);
            ps.setString(2, tokenHash);
            ps.setString(3, Banco.gravar(criadoEm));
            ps.setString(4, Banco.gravar(expiraEm));
            ps.executeUpdate();
        }
    }

    // Retorna null se não encontrar.
    public Sessao buscarPorTokenHash(String tokenHash) throws SQLException {
        try (Connection c = banco.conectar();
                PreparedStatement ps = c.prepareStatement(
                        "SELECT id_sessao, id_usuario, expira_em, revogada_em FROM sessao WHERE token_hash = ?")) {
            ps.setString(1, tokenHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                String revogada = rs.getString("revogada_em");
                return new Sessao(rs.getInt("id_sessao"), rs.getInt("id_usuario"),
                        Banco.ler(rs.getString("expira_em")),
                        revogada == null ? null : Banco.ler(revogada));
            }
        }
    }

    public void revogar(int sessaoId) throws SQLException {
        try (Connection c = banco.conectar();
                PreparedStatement ps = c.prepareStatement(
                        "UPDATE sessao SET revogada_em = ? WHERE id_sessao = ? AND revogada_em IS NULL")) {
            ps.setString(1, Banco.gravar(LocalDateTime.now()));
            ps.setInt(2, sessaoId);
            ps.executeUpdate();
        }
    }
}
