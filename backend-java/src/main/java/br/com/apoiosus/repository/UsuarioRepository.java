package br.com.apoiosus.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import br.com.apoiosus.Banco;
import br.com.apoiosus.exception.ApiException;
import br.com.apoiosus.model.Usuario;

public class UsuarioRepository {

    private static final String COLUNAS = "id_usuario, nome, email, senha, data_nascimento";

    private final Banco banco;

    public UsuarioRepository(Banco banco) {
        this.banco = banco;
    }

    public boolean existePorEmail(String email) throws SQLException {
        try (Connection c = banco.conectar();
                PreparedStatement ps = c.prepareStatement("SELECT 1 FROM usuario WHERE email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Retorna null se não encontrar.
    public Usuario buscarPorEmail(String email) throws SQLException {
        try (Connection c = banco.conectar();
                PreparedStatement ps = c.prepareStatement("SELECT " + COLUNAS + " FROM usuario WHERE email = ?")) {
            ps.setString(1, email);
            return lerUm(ps);
        }
    }

    public Usuario buscarPorId(int id) throws SQLException {
        try (Connection c = banco.conectar();
                PreparedStatement ps = c.prepareStatement("SELECT " + COLUNAS + " FROM usuario WHERE id_usuario = ?")) {
            ps.setInt(1, id);
            return lerUm(ps);
        }
    }

    // Salva o usuário e o perfil de saúde vazio na mesma transação: se um falhar, nenhum é salvo.
    public Usuario inserirComPerfil(String nome, String email, String senhaHash, String dataNascimento)
            throws SQLException {
        try (Connection c = banco.conectar()) {
            c.setAutoCommit(false);

            int id;
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO usuario (nome, email, senha, data_nascimento) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, nome);
                ps.setString(2, email);
                ps.setString(3, senhaHash);
                ps.setString(4, dataNascimento);
                ps.executeUpdate();
                try (ResultSet chaves = ps.getGeneratedKeys()) {
                    chaves.next();
                    id = chaves.getInt(1);
                }
            }

            try (PreparedStatement ps = c.prepareStatement("INSERT INTO perfil_saude (id_usuario) VALUES (?)")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            c.commit();
            return new Usuario(id, nome, email, senhaHash, dataNascimento);
        } catch (SQLException e) {
            // Dois cadastros ao mesmo tempo com o mesmo e-mail: o banco recusa o segundo (UNIQUE).
            if (Banco.ehChaveDuplicada(e)) {
                throw new ApiException(409, "EMAIL_JA_CADASTRADO", "Este e-mail já está cadastrado.");
            }
            throw e;
        }
    }

    public void atualizar(int id, String nome, String dataNascimento) throws SQLException {
        try (Connection c = banco.conectar();
                PreparedStatement ps = c.prepareStatement(
                        "UPDATE usuario SET nome = ?, data_nascimento = ? WHERE id_usuario = ?")) {
            ps.setString(1, nome);
            ps.setString(2, dataNascimento);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    // As tabelas filhas (perfil, sessões, vacinas...) usam ON DELETE CASCADE e são apagadas junto.
    public void excluir(int id) throws SQLException {
        try (Connection c = banco.conectar();
                PreparedStatement ps = c.prepareStatement("DELETE FROM usuario WHERE id_usuario = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Usuario lerUm(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                return null;
            }
            return new Usuario(rs.getInt("id_usuario"), rs.getString("nome"), rs.getString("email"),
                    rs.getString("senha"), rs.getString("data_nascimento"));
        }
    }
}
