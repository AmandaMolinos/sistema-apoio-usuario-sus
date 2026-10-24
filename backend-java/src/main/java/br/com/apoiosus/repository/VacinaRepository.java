package br.com.apoiosus.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.com.apoiosus.Banco;
import br.com.apoiosus.model.Vacina;

// Todas as consultas por id também filtram pelo dono (id_usuario):
// assim um usuário nunca lê, altera ou apaga a vacina de outro.
public class VacinaRepository {

    private static final String COLUNAS = "id_vacina, nome_vacina, data_aplicacao, lote, local_aplicacao";

    private final Banco banco;

    public VacinaRepository(Banco banco) {
        this.banco = banco;
    }

    // Da mais recente para a mais antiga.
    public List<Vacina> listar(int usuarioId) throws SQLException {
        try (Connection c = banco.conectar();
                PreparedStatement ps = c.prepareStatement("SELECT " + COLUNAS + " FROM vacina WHERE id_usuario = ? "
                        + "ORDER BY data_aplicacao DESC, id_vacina DESC")) {
            ps.setInt(1, usuarioId);
            List<Vacina> vacinas = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    vacinas.add(ler(rs));
                }
            }
            return vacinas;
        }
    }

    // Retorna null se não existir ou se pertencer a outro usuário.
    public Vacina buscar(int usuarioId, int id) throws SQLException {
        try (Connection c = banco.conectar();
                PreparedStatement ps = c.prepareStatement(
                        "SELECT " + COLUNAS + " FROM vacina WHERE id_vacina = ? AND id_usuario = ?")) {
            ps.setInt(1, id);
            ps.setInt(2, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? ler(rs) : null;
            }
        }
    }

    public int inserir(int usuarioId, String nome, String data, String lote, String local) throws SQLException {
        try (Connection c = banco.conectar();
                PreparedStatement ps = c.prepareStatement("INSERT INTO vacina (id_usuario, nome_vacina, "
                        + "data_aplicacao, lote, local_aplicacao) VALUES (?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, usuarioId);
            ps.setString(2, nome);
            ps.setString(3, data);
            ps.setString(4, lote);
            ps.setString(5, local);
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                return chaves.getInt(1);
            }
        }
    }

    // Retorna false se a vacina não existe ou não é do usuário.
    public boolean atualizar(int usuarioId, int id, String nome, String data, String lote, String local)
            throws SQLException {
        try (Connection c = banco.conectar();
                PreparedStatement ps = c.prepareStatement("UPDATE vacina SET nome_vacina = ?, data_aplicacao = ?, "
                        + "lote = ?, local_aplicacao = ? WHERE id_vacina = ? AND id_usuario = ?")) {
            ps.setString(1, nome);
            ps.setString(2, data);
            ps.setString(3, lote);
            ps.setString(4, local);
            ps.setInt(5, id);
            ps.setInt(6, usuarioId);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean excluir(int usuarioId, int id) throws SQLException {
        try (Connection c = banco.conectar();
                PreparedStatement ps = c.prepareStatement("DELETE FROM vacina WHERE id_vacina = ? AND id_usuario = ?")) {
            ps.setInt(1, id);
            ps.setInt(2, usuarioId);
            return ps.executeUpdate() == 1;
        }
    }

    private Vacina ler(ResultSet rs) throws SQLException {
        return new Vacina(rs.getInt("id_vacina"), rs.getString("nome_vacina"), rs.getString("data_aplicacao"),
                rs.getString("lote"), rs.getString("local_aplicacao"));
    }
}
