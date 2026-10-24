package br.com.apoiosus;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// Conexão com o banco (SQLite ou MySQL, conforme o início da URL JDBC) e criação das tabelas (migrations).
// Cada banco tem a sua pasta de migrations em resources/db/migration/<sqlite|mysql>, com os mesmos nomes de arquivo.
public class Banco {

    // Em ordem. Para mudar o banco, crie um novo arquivo V<n> em cada pasta e acrescente aqui.
    private static final String[] MIGRATIONS = {
            "V1__schema_inicial.sql", "V2__sessao.sql", "V3__recuperacao_senha.sql" };

    // Data e hora gravadas como texto "aaaa-mm-dd hh:mm:ss.ffffff": os dois bancos aceitam, e o MySQL
    // guarda no máximo 6 casas decimais de segundo.
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    private final String url;
    private final String pasta;

    public Banco(String url) {
        this.url = url;
        this.pasta = url.startsWith("jdbc:mysql:") ? "mysql" : "sqlite";
    }

    public Connection conectar() throws SQLException {
        return DriverManager.getConnection(url);
    }

    public boolean ehMysql() {
        return "mysql".equals(pasta);
    }

    // Texto para gravar uma data e hora numa coluna TIMESTAMP/DATETIME.
    public static String gravar(LocalDateTime dataHora) {
        return DATA_HORA.format(dataHora);
    }

    // Lê uma data e hora gravada por este banco (aceita o "T" do formato antigo e o espaço do MySQL).
    public static LocalDateTime ler(String texto) {
        return LocalDateTime.parse(texto.replace(' ', 'T'));
    }

    // O banco recusou por chave duplicada (UNIQUE)? SQLite e MySQL usam mensagens e códigos diferentes.
    public static boolean ehChaveDuplicada(SQLException e) {
        String mensagem = e.getMessage();
        return e.getErrorCode() == 1062 || (mensagem != null
                && (mensagem.contains("UNIQUE constraint failed") || mensagem.contains("Duplicate entry")));
    }

    // Roda só as migrations que ainda não foram aplicadas (controle na tabela schema_migracao).
    public void criarTabelas() throws Exception {
        try (Connection c = conectar(); Statement st = c.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS schema_migracao ("
                    + "versao INTEGER PRIMARY KEY, aplicada_em " + (ehMysql() ? "DATETIME(6)" : "TIMESTAMP")
                    + " NOT NULL)");

            for (int i = 0; i < MIGRATIONS.length; i++) {
                int versao = i + 1;
                if (jaAplicada(c, versao)) {
                    continue;
                }
                c.setAutoCommit(false);
                for (String comando : lerComandos(MIGRATIONS[i])) {
                    st.execute(comando);
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO schema_migracao (versao, aplicada_em) VALUES (?, ?)")) {
                    ps.setInt(1, versao);
                    ps.setString(2, gravar(LocalDateTime.now()));
                    ps.executeUpdate();
                }
                c.commit();
                c.setAutoCommit(true);
            }
        }
    }

    private boolean jaAplicada(Connection c, int versao) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM schema_migracao WHERE versao = ?")) {
            ps.setInt(1, versao);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Lê o arquivo .sql, ignora linhas de comentário (--) e separa os comandos por ';'.
    private List<String> lerComandos(String arquivo) throws Exception {
        try (InputStream in = Banco.class.getClassLoader().getResourceAsStream("db/migration/" + pasta + "/" + arquivo)) {
            String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            StringBuilder semComentarios = new StringBuilder();
            for (String linha : sql.split("\\R")) {
                if (!linha.trim().startsWith("--")) {
                    semComentarios.append(linha).append('\n');
                }
            }
            List<String> comandos = new ArrayList<>();
            for (String parte : semComentarios.toString().split(";")) {
                if (!parte.isBlank()) {
                    comandos.add(parte.trim());
                }
            }
            return comandos;
        }
    }
}
