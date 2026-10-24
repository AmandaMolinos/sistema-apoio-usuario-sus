package br.com.apoiosus;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;

import br.com.apoiosus.integration.CnesClient;
import br.com.apoiosus.service.RecuperacaoSenhaService;

// Sobe a aplicação real numa porta livre, com um banco SQLite temporário, e oferece chamadas HTTP.
class ServidorDeTeste implements AutoCloseable {

	record Resp(int status, String corpo) {
		JsonObject json() {
			return JsonParser.parseString(corpo).getAsJsonObject();
		}

		JsonArray lista() {
			return JsonParser.parseString(corpo).getAsJsonArray();
		}

		String texto(String caminho) {
			JsonObject atual = json();
			String[] partes = caminho.split("\\.");
			for (int i = 0; i < partes.length - 1; i++) {
				atual = atual.getAsJsonObject(partes[i]);
			}
			var valor = atual.get(partes[partes.length - 1]);
			return valor == null || valor.isJsonNull() ? null : valor.getAsString();
		}
	}

	static final String SENHA = "senha1234";

	// Códigos de recuperação que o servidor escreveu no log: e-mail -> último código.
	final Map<String, String> codigosDeRecuperacao = new ConcurrentHashMap<>();

	private static final Pattern LINHA_DO_LOG = Pattern.compile("e-mail=(\\S+) codigo=(\\S+)");

	private final HttpServer servidor;
	private final HttpClient cliente = HttpClient.newHttpClient();
	private final String dbUrl;
	private final Logger logRecuperacao = Logger.getLogger(RecuperacaoSenhaService.class.getName());
	private final Handler capturaDoLog = new Handler() {
		@Override
		public void publish(LogRecord registro) {
			Matcher m = LINHA_DO_LOG.matcher(registro.getMessage());
			if (m.find()) {
				codigosDeRecuperacao.put(m.group(1), m.group(2));
			}
		}

		@Override
		public void flush() {
		}

		@Override
		public void close() {
		}
	};

	ServidorDeTeste(long horasValidadeSessao) throws Exception {
		this(horasValidadeSessao, 30);
	}

	ServidorDeTeste(long horasValidadeSessao, long minutosValidadeRecuperacao) throws Exception {
		this(horasValidadeSessao, minutosValidadeRecuperacao, null);
	}

	// Com um cliente do CNES apontando para um servidor falso; null usa o endereço real (que os testes não chamam).
	ServidorDeTeste(CnesClient cnes) throws Exception {
		this(24, 30, cnes);
	}

	private ServidorDeTeste(long horasValidadeSessao, long minutosValidadeRecuperacao, CnesClient cnes)
			throws Exception {
		// Com TESTE_MYSQL_URL definida (ex.: jdbc:mysql://localhost:3306/apoio_sus_teste?user=...&password=...),
		// os testes rodam num banco MySQL, que é esvaziado antes de cada servidor. Sem ela, usam SQLite temporário.
		String mysql = System.getenv("TESTE_MYSQL_URL");
		if (mysql != null && !mysql.isBlank()) {
			this.dbUrl = mysql;
			esvaziarMysql(mysql);
		} else {
			Path pasta = Path.of("target", "testes");
			Files.createDirectories(pasta);
			Path arquivo = pasta.resolve("apoio-sus-" + UUID.randomUUID() + ".db");
			this.dbUrl = "jdbc:sqlite:" + arquivo + "?foreign_keys=on&busy_timeout=5000";
		}
		logRecuperacao.addHandler(capturaDoLog);
		this.servidor = cnes == null
				? ApoioSusApplication.iniciar(0, dbUrl, horasValidadeSessao, minutosValidadeRecuperacao)
				: ApoioSusApplication.iniciar(0, dbUrl, horasValidadeSessao, minutosValidadeRecuperacao, cnes);
	}

	ServidorDeTeste() throws Exception {
		this(24);
	}

	// Apaga todas as tabelas do banco MySQL de teste, para cada teste começar do zero.
	private static void esvaziarMysql(String url) throws Exception {
		try (Connection c = DriverManager.getConnection(url); var st = c.createStatement()) {
			st.execute("SET FOREIGN_KEY_CHECKS = 0");
			var tabelas = new java.util.ArrayList<String>();
			try (ResultSet rs = st.executeQuery("SELECT table_name FROM information_schema.tables "
					+ "WHERE table_schema = DATABASE()")) {
				while (rs.next()) {
					tabelas.add(rs.getString(1));
				}
			}
			for (String tabela : tabelas) {
				st.execute("DROP TABLE `" + tabela + "`");
			}
			st.execute("SET FOREIGN_KEY_CHECKS = 1");
		}
	}

	Resp get(String caminho, String token) throws Exception {
		return enviar("GET", caminho, null, token);
	}

	Resp post(String caminho, String json, String token) throws Exception {
		return enviar("POST", caminho, json, token);
	}

	Resp put(String caminho, String json, String token) throws Exception {
		return enviar("PUT", caminho, json, token);
	}

	Resp delete(String caminho, String token) throws Exception {
		return enviar("DELETE", caminho, null, token);
	}

	private Resp enviar(String metodo, String caminho, String json, String token) throws Exception {
		var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + servidor.getAddress().getPort() + caminho));
		if (json != null) {
			builder.header("Content-Type", "application/json");
		}
		if (token != null) {
			builder.header("Authorization", "Bearer " + token);
		}
		builder.method(metodo, json == null ? HttpRequest.BodyPublishers.noBody()
				: HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8));
		var resposta = cliente.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		return new Resp(resposta.statusCode(), resposta.body());
	}

	String emailUnico() {
		return "teste-" + UUID.randomUUID() + "@exemplo.com";
	}

	Resp cadastrar(String nome, String email, String senha, String dataNascimento) throws Exception {
		String data = dataNascimento == null ? "null" : "\"" + dataNascimento + "\"";
		return post("/api/auth/cadastro", """
				{"nome":"%s","email":"%s","senha":"%s","dataNascimento":%s}""".formatted(nome, email, senha, data), null);
	}

	Resp login(String email, String senha) throws Exception {
		return post("/api/auth/login", """
				{"email":"%s","senha":"%s"}""".formatted(email, senha), null);
	}

	String cadastrarELogar() throws Exception {
		String email = emailUnico();
		cadastrar("Usuario", email, SENHA, null);
		return login(email, SENHA).texto("token");
	}

	boolean ehMysql() {
		return dbUrl.startsWith("jdbc:mysql:");
	}

	int contar(String sql, Object... parametros) throws Exception {
		try (Connection c = DriverManager.getConnection(dbUrl);
				PreparedStatement ps = c.prepareStatement(sql)) {
			for (int i = 0; i < parametros.length; i++) {
				ps.setObject(i + 1, parametros[i]);
			}
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1);
			}
		}
	}

	String textoDoBanco(String sql, Object... parametros) throws Exception {
		try (Connection c = DriverManager.getConnection(dbUrl);
				PreparedStatement ps = c.prepareStatement(sql)) {
			for (int i = 0; i < parametros.length; i++) {
				ps.setObject(i + 1, parametros[i]);
			}
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getString(1);
			}
		}
	}

	@Override
	public void close() {
		logRecuperacao.removeHandler(capturaDoLog);
		servidor.stop(0);
	}
}
