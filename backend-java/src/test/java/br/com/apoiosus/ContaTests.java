package br.com.apoiosus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import br.com.apoiosus.ServidorDeTeste.Resp;

class ContaTests {

	static ServidorDeTeste api;

	@BeforeAll
	static void subir() throws Exception {
		api = new ServidorDeTeste();
	}

	@AfterAll
	static void parar() {
		api.close();
	}

	private Resp atualizar(String token, String nome, String data) {
		try {
			String d = data == null ? "null" : "\"" + data + "\"";
			return api.put("/api/usuarios/me", "{\"nome\":\"" + nome + "\",\"dataNascimento\":" + d + "}", token);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	void atualizarAlteraNomeEDataEMantemOEmail() throws Exception {
		String email = api.emailUnico();
		api.cadastrar("Nome Antigo", email, ServidorDeTeste.SENHA, null);
		String token = api.login(email, ServidorDeTeste.SENHA).texto("token");

		Resp r = atualizar(token, "Nome Novo", "1985-12-31");

		assertEquals(200, r.status());
		assertEquals("Nome Novo", r.texto("nome"));
		assertEquals("1985-12-31", r.texto("dataNascimento"));
		assertEquals(email, r.texto("email"));

		Resp me = api.get("/api/usuarios/me", token);
		assertEquals("Nome Novo", me.texto("nome"));
		assertEquals("1985-12-31", me.texto("dataNascimento"));
	}

	@Test
	void dataVaziaLimpaADataDeNascimento() throws Exception {
		String email = api.emailUnico();
		api.cadastrar("Ana", email, ServidorDeTeste.SENHA, "1990-01-01");
		String token = api.login(email, ServidorDeTeste.SENHA).texto("token");

		Resp r = atualizar(token, "Ana", null);

		assertEquals(200, r.status());
		assertNull(r.texto("dataNascimento"));
	}

	@Test
	void dadosInvalidosRetornamErroPorCampo() throws Exception {
		String token = api.cadastrarELogar();

		Resp r = atualizar(token, "", "2999-01-01");

		assertEquals(400, r.status());
		assertEquals("VALIDACAO", r.texto("codigo"));
		assertNotNull(r.texto("erros.nome"));
		assertNotNull(r.texto("erros.dataNascimento"));

		assertEquals("Data inválida. Use o formato aaaa-mm-dd.",
				atualizar(token, "Ana", "ontem").texto("erros.dataNascimento"));
	}

	@Test
	void semTokenRetorna401() throws Exception {
		assertEquals(401, atualizar(null, "Ana", null).status());
		assertEquals(401, api.delete("/api/usuarios/me", null).status());
	}

	@Test
	void metodoNaoPermitidoRetorna405() throws Exception {
		assertEquals(405, api.post("/api/usuarios/me", "{}", null).status());
	}

	@Test
	void excluirContaApagaOsDadosDoUsuarioEPreservaOsDeOutros() throws Exception {
		String email = api.emailUnico();
		api.cadastrar("Ana", email, ServidorDeTeste.SENHA, null);
		String token = api.login(email, ServidorDeTeste.SENHA).texto("token");
		String outroEmail = api.emailUnico();
		api.cadastrar("Outro", outroEmail, ServidorDeTeste.SENHA, null);

		assertEquals(204, api.delete("/api/usuarios/me", token).status());

		assertEquals(401, api.get("/api/usuarios/me", token).status());
		assertEquals(401, api.login(email, ServidorDeTeste.SENHA).status());
		assertEquals(0, api.contar("SELECT COUNT(*) FROM usuario WHERE email = ?", email));
		assertEquals(0, api.contar("SELECT COUNT(*) FROM sessao s LEFT JOIN usuario u "
				+ "ON u.id_usuario = s.id_usuario WHERE u.id_usuario IS NULL"));
		assertEquals(0, api.contar("SELECT COUNT(*) FROM perfil_saude p LEFT JOIN usuario u "
				+ "ON u.id_usuario = p.id_usuario WHERE u.id_usuario IS NULL"));
		assertEquals(200, api.login(outroEmail, ServidorDeTeste.SENHA).status());
	}
}
