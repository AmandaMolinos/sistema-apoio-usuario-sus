package br.com.apoiosus.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import br.com.apoiosus.Banco;

public class RecuperacaoSenhaRepository {

    private final Banco banco;

    public RecuperacaoSenhaRepository(Banco banco) {
        this.banco = banco;
    }

    // Um novo pedido invalida os códigos anteriores ainda não usados e grava o novo (mesma transação).
    public void substituirCodigo(int usuarioId, String tokenHash, LocalDateTime expiraEm) throws SQLException {
        String agora = Banco.gravar(LocalDateTime.now());
        try (Connection c = banco.conectar()) {
            c.setAutoCommit(false);

            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE recuperacao_senha SET usado_em = ? WHERE id_usuario = ? AND usado_em IS NULL")) {
                ps.setString(1, agora);
                ps.setInt(2, usuarioId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = c.prepareStatement("INSERT INTO recuperacao_senha "
                    + "(id_usuario, token_hash, criado_em, expira_em) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, usuarioId);
                ps.setString(2, tokenHash);
                ps.setString(3, agora);
                ps.setString(4, Banco.gravar(expiraEm));
                ps.executeUpdate();
            }

            c.commit();
        }
    }

    // Usa o código para trocar a senha. Tudo na mesma transação:
    // 1) marca o código como usado, 2) grava a nova senha, 3) encerra todas as sessões do usuário.
    // Retorna false se o código não existe, já foi usado ou expirou.
    public boolean redefinirSenha(String tokenHash, String novaSenhaHash) throws SQLException {
        try (Connection c = banco.conectar()) {
            c.setAutoCommit(false);

            int recuperacaoId;
            int usuarioId;
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT id_recuperacao, id_usuario, expira_em, usado_em FROM recuperacao_senha WHERE token_hash = ?")) {
                ps.setString(1, tokenHash);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return false;
                    }
                    boolean jaUsado = rs.getString("usado_em") != null;
                    boolean expirado = !Banco.ler(rs.getString("expira_em")).isAfter(LocalDateTime.now());
                    if (jaUsado || expirado) {
                        return false;
                    }
                    recuperacaoId = rs.getInt("id_recuperacao");
                    usuarioId = rs.getInt("id_usuario");
                }
            }

            String agora = Banco.gravar(LocalDateTime.now());

            // "AND usado_em IS NULL" garante que dois pedidos ao mesmo tempo não usem o código duas vezes.
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE recuperacao_senha SET usado_em = ? WHERE id_recuperacao = ? AND usado_em IS NULL")) {
                ps.setString(1, agora);
                ps.setInt(2, recuperacaoId);
                if (ps.executeUpdate() != 1) {
                    return false;
                }
            }

            try (PreparedStatement ps = c.prepareStatement("UPDATE usuario SET senha = ? WHERE id_usuario = ?")) {
                ps.setString(1, novaSenhaHash);
                ps.setInt(2, usuarioId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE sessao SET revogada_em = ? WHERE id_usuario = ? AND revogada_em IS NULL")) {
                ps.setString(1, agora);
                ps.setInt(2, usuarioId);
                ps.executeUpdate();
            }

            c.commit();
            return true;
        }
    }
}
