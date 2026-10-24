package br.com.apoiosus.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import br.com.apoiosus.Banco;
import br.com.apoiosus.dto.response.PerfilSaudeResponse;

// O perfil é criado vazio junto com o usuário (UsuarioRepository.inserirComPerfil). Aqui só lemos e alteramos.
public class PerfilSaudeRepository {

    private final Banco banco;

    public PerfilSaudeRepository(Banco banco) {
        this.banco = banco;
    }

    // Retorna null se o usuário não tiver perfil.
    public PerfilSaudeResponse buscar(int usuarioId) throws SQLException {
        try (Connection c = banco.conectar();
                PreparedStatement ps = c.prepareStatement("SELECT id_perfil, id_usuario, peso, altura, "
                        + "tipo_sanguineo, alergias, observacoes_medicas FROM perfil_saude WHERE id_usuario = ?")) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new PerfilSaudeResponse(rs.getInt("id_perfil"), rs.getInt("id_usuario"),
                        lerDouble(rs, "peso"), lerDouble(rs, "altura"), rs.getString("tipo_sanguineo"),
                        rs.getString("alergias"), rs.getString("observacoes_medicas"));
            }
        }
    }

    public void atualizar(int usuarioId, Double peso, Double altura, String tipoSanguineo, String alergias,
            String observacoes) throws SQLException {
        try (Connection c = banco.conectar();
                PreparedStatement ps = c.prepareStatement("UPDATE perfil_saude SET peso = ?, altura = ?, "
                        + "tipo_sanguineo = ?, alergias = ?, observacoes_medicas = ? WHERE id_usuario = ?")) {
            ps.setObject(1, peso);
            ps.setObject(2, altura);
            ps.setString(3, tipoSanguineo);
            ps.setString(4, alergias);
            ps.setString(5, observacoes);
            ps.setInt(6, usuarioId);
            ps.executeUpdate();
        }
    }

    // Limpa os dados mas mantém a linha do perfil (a relação com a conta continua).
    public void limpar(int usuarioId) throws SQLException {
        atualizar(usuarioId, null, null, null, null, null);
    }

    private Double lerDouble(ResultSet rs, String coluna) throws SQLException {
        double valor = rs.getDouble(coluna);
        return rs.wasNull() ? null : valor;
    }
}
