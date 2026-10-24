package br.com.apoiosus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import br.com.apoiosus.ServidorDeTeste.Resp;

class ApiTests {

	static ServidorDeTeste api;

	@BeforeAll
	static void subir() throws Exception {
		api = new ServidorDeTeste();
	}

	@AfterAll
	static void parar() {
		api.close();
	}

	// ---- Infra / roteamento ----

	@Test
	void migrationCriaAsTabelasPrincipais() throws Exception {
		String nomes = "('usuario','perfil_saude','vacina','medicamento','unidade_saude',"
				+ "'campanha_vacinacao','orientacao_saude','analise_sintoma','sessao')";
		int tabelas = api.ehMysql()
				? api.contar("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() "
						+ "AND table_name IN " + nomes)
				: api.contar("SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name IN " + nomes);
		assertEquals(9, tabelas);
	}

	@Test
	void pingEPublico() throws Exception {
		Resp r = api.get("/api/ping", null);
		assertEquals(200, r.status());
		assertEquals("ok", r.texto("status"));
	}

	@Test
	void rotaInexistenteRetorna404eMetodoErradoRetorna405() throws Exception {
		assertEquals(404, api.get("/api/nao-existe", null).status());
		assertEquals(405, api.post("/api/ping", "{}", null).status());
	}

	@Test
	void semTokenOuComTokenInvalidoRetorna401() throws Exception {
		Resp semToken = api.get("/api/usuarios/me", null);
		assertEquals(401, semToken.status());
		assertEquals("NAO_AUTENTICADO", semToken.texto("codigo"));
		assertEquals(401, api.get("/api/usuarios/me", "token-inventado").status());
	}

	// ---- Cadastro ----

	@Test
	void cadastroValidoCriaUsuarioEPerfilVazioSemExporSenha() throws Exception {
		String email = api.emailUnico();

		Resp r = api.cadastrar("Maria Silva", email.toUpperCase(), "senha1234", "1990-05-20");

		assertEquals(201, r.status());
		assertEquals(email, r.texto("email"));
		assertFalse(r.json().has("senha"));
		assertFalse(r.json().has("senhaHash"));

		String hash = api.textoDoBanco("SELECT senha FROM usuario WHERE email = ?", email);
		assertTrue(hash.startsWith("pbkdf2_sha256$"));
		assertFalse(hash.contains("senha1234"));

		assertEquals(1, api.contar("SELECT COUNT(*) FROM perfil_saude p JOIN usuario u "
				+ "ON u.id_usuario = p.id_usuario WHERE u.email = ?", email));
	}

	@Test
	void emailDuplicadoIgnorandoMaiusculasRetorna409() throws Exception {
		String email = api.emailUnico();
		assertEquals(201, api.cadastrar("Maria", email, "senha1234", null).status());

		Resp r = api.cadastrar("Outra Maria", email.toUpperCase(), "senha1234", null);

		assertEquals(409, r.status());
		assertEquals("EMAIL_JA_CADASTRADO", r.texto("codigo"));
	}

	@Test
	void camposInvalidosRetornamErroPorCampo() throws Exception {
		Resp r = api.cadastrar("", "nao-e-email", "curta", LocalDate.now().plusDays(1).toString());

		assertEquals(400, r.status());
		assertEquals("VALIDACAO", r.texto("codigo"));
		assertNotNull(r.texto("erros.nome"));
		assertNotNull(r.texto("erros.email"));
		assertNotNull(r.texto("erros.senha"));
		assertNotNull(r.texto("erros.dataNascimento"));

		// Formato ProblemDetail (igual ao do backend Spring) convivendo com os campos próprios.
		assertEquals("400", r.texto("status"));
		assertEquals("Bad Request", r.texto("title"));
		assertEquals("about:blank", r.texto("type"));
		assertTrue(r.texto("detail").contains("email: E-mail inválido."));
		assertTrue(r.texto("detail").contains("; "));
	}

	@Test
	void corpoAusenteOuMalformadoRetorna400() throws Exception {
		Resp ausente = api.post("/api/auth/cadastro", null, null);
		assertEquals(400, ausente.status());
		assertEquals("REQUISICAO_INVALIDA", ausente.texto("codigo"));

		assertEquals(400, api.post("/api/auth/cadastro", "{nao-e-json", null).status());
		assertEquals(400, api.post("/api/auth/cadastro",
				"{\"nome\":\"A\",\"email\":\"a@b.com\",\"senha\":\"senha1234\",\"dataNascimento\":\"ontem\"}", null).status());
	}

	// ---- Login ----

	@Test
	void loginValidoRetornaTokenQueDaAcessoAoUsuarioLogado() throws Exception {
		String email = api.emailUnico();
		api.cadastrar("Joao", email, ServidorDeTeste.SENHA, null);

		Resp login = api.login(email.toUpperCase(), ServidorDeTeste.SENHA);
		assertEquals(200, login.status());
		assertNotNull(login.texto("token"));
		assertNotNull(login.texto("expiraEm"));
		assertEquals("Bearer", login.texto("tipo"));
		assertEquals("86400", login.texto("expiresIn"));
		assertEquals(email, login.texto("usuario.email"));

		Resp me = api.get("/api/usuarios/me", login.texto("token"));
		assertEquals(200, me.status());
		assertEquals(email, me.texto("email"));
		assertNull(me.texto("dataNascimento"));
	}

	@Test
	void bancoGuardaSomenteOHashDoToken() throws Exception {
		String token = api.cadastrarELogar();

		assertEquals(0, api.contar("SELECT COUNT(*) FROM sessao WHERE token_hash = ?", token));
		assertTrue(api.contar("SELECT COUNT(*) FROM sessao WHERE length(token_hash) = 64") > 0);
	}

	@Test
	void senhaErradaEEmailInexistenteRetornamAMesmaMensagem() throws Exception {
		String email = api.emailUnico();
		api.cadastrar("Joao", email, ServidorDeTeste.SENHA, null);

		Resp senhaErrada = api.login(email, "senha-errada");
		Resp semConta = api.login("nao-existe@exemplo.com", ServidorDeTeste.SENHA);

		assertEquals(401, senhaErrada.status());
		assertEquals(401, semConta.status());
		assertEquals("CREDENCIAIS_INVALIDAS", senhaErrada.texto("codigo"));
		assertEquals("E-mail ou senha incorretos.", senhaErrada.texto("mensagem"));
		assertEquals(senhaErrada.corpo(), semConta.corpo());
	}

	@Test
	void loginComCamposVaziosRetornaErroDeValidacao() throws Exception {
		Resp r = api.login("", "");
		assertEquals(400, r.status());
		assertNotNull(r.texto("erros.email"));
		assertNotNull(r.texto("erros.senha"));
	}

	// ---- Logout ----

	@Test
	void logoutRevogaOTokenUsado() throws Exception {
		String token = api.cadastrarELogar();

		assertEquals(204, api.post("/api/auth/logout", null, token).status());

		Resp depois = api.get("/api/usuarios/me", token);
		assertEquals(401, depois.status());
		assertEquals("NAO_AUTENTICADO", depois.texto("codigo"));
	}

	@Test
	void logoutNaoAfetaOutraSessaoDoMesmoUsuario() throws Exception {
		String email = api.emailUnico();
		api.cadastrar("Rita", email, ServidorDeTeste.SENHA, null);
		String tokenA = api.login(email, ServidorDeTeste.SENHA).texto("token");
		String tokenB = api.login(email, ServidorDeTeste.SENHA).texto("token");
		assertNotEquals(tokenA, tokenB);

		assertEquals(204, api.post("/api/auth/logout", null, tokenA).status());

		assertEquals(401, api.get("/api/usuarios/me", tokenA).status());
		assertEquals(200, api.get("/api/usuarios/me", tokenB).status());
	}

	@Test
	void logoutSemTokenOuRepetidoRetorna401() throws Exception {
		assertEquals(401, api.post("/api/auth/logout", null, null).status());

		String token = api.cadastrarELogar();
		assertEquals(204, api.post("/api/auth/logout", null, token).status());
		assertEquals(401, api.post("/api/auth/logout", null, token).status());
	}
}
