package br.com.apoiosus.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.com.apoiosus.Banco;
import br.com.apoiosus.model.Medicamento;

// Todas as consultas por id também filtram pelo dono (id_usuario):
// assim um usuário nunca lê, altera ou apaga o medicamento de outro.
public class MedicamentoRepository {

    private static final String COLUNAS = "id_medicamento, nome_medicamento, dosagem, frequencia, data_inicio, data_fim";

    private final Banco banco;

    public MedicamentoRepository(Banco banco) {
        this.banco = banco;
    }

    // Em ordem alfabética do nome.
    public List<Medicamento> listar(int usuarioId) throws SQLException {
        try (Connection c = banco.conectar();
                PreparedStatement ps = c.prepareStatement("SELECT " + COLUNAS + " FROM medicamento "
                        + "WHERE id_usuario = ? ORDER BY nome_medicamento" + ordemSemMaiusculas()
                        + ", id_medicamento")) {
            ps.setInt(1, usuarioId);
            List<Medicamento> medicamentos = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    medicamentos.add(ler(rs));
                }
            }
            return medicamentos;
        }
    }

    // O SQLite ordena diferenciando maiúsculas; o MySQL, com a collation padrão, já não diferencia.
    private String ordemSemMaiusculas() {
        return banco.ehMysql() ? "" : " COLLATE NOCASE";
    }

    // Retorna null se não existir ou se pertencer a outro usuário.
    public Medicamento buscar(int usuarioId, int id) throws SQLException {
        try (Connection c = banco.conectar();
                PreparedStatement ps = c.prepareStatement(
                        "SELECT " + COLUNAS + " FROM medicamento WHERE id_medicamento = ? AND id_usuario = ?")) {
            ps.setInt(1, id);
            ps.setInt(2, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? ler(rs) : null;
            }
        }
    }

    public int inserir(int usuarioId, String nome, String dosagem, String frequencia, String inicio, String fim)
            throws SQLException {
        try (Connection c = banco.conectar();
                PreparedStatement ps = c.prepareStatement("INSERT INTO medicamento (id_usuario, nome_medicamento, "
                        + "dosagem, frequencia, data_inicio, data_fim) VALUES (?, ?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, usuarioId);
            ps.setString(2, nome);
            ps.setString(3, dosagem);
            ps.setString(4, frequencia);
            ps.setString(5, inicio);
            ps.setString(6, fim);
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                return chaves.getInt(1);
            }
        }
    }

    // Retorna false se o medicamento não existe ou não é do usuário.
    public boolean atualizar(int usuarioId, int id, String nome, String dosagem, String frequencia, String inicio,
            String fim) throws SQLException {
        try (Connection c = banco.conectar();
                PreparedStatement ps = c.prepareStatement("UPDATE medicamento SET nome_medicamento = ?, dosagem = ?, "
                        + "frequencia = ?, data_inicio = ?, data_fim = ? WHERE id_medicamento = ? AND id_usuario = ?")) {
            ps.setString(1, nome);
            ps.setString(2, dosagem);
            ps.setString(3, frequencia);
            ps.setString(4, inicio);
            ps.setString(5, fim);
            ps.setInt(6, id);
            ps.setInt(7, usuarioId);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean excluir(int usuarioId, int id) throws SQLException {
        try (Connection c = banco.conectar();
                PreparedStatement ps = c.prepareStatement(
                        "DELETE FROM medicamento WHERE id_medicamento = ? AND id_usuario = ?")) {
            ps.setInt(1, id);
            ps.setInt(2, usuarioId);
            return ps.executeUpdate() == 1;
        }
    }

    private Medicamento ler(ResultSet rs) throws SQLException {
        return new Medicamento(rs.getInt("id_medicamento"), rs.getString("nome_medicamento"), rs.getString("dosagem"),
                rs.getString("frequencia"), rs.getString("data_inicio"), rs.getString("data_fim"));
    }
}
