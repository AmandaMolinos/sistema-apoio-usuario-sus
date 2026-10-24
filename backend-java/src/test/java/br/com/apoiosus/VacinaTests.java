package br.com.apoiosus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import br.com.apoiosus.ServidorDeTeste.Resp;

class VacinaTests {

	static final String ROTA = "/api/vacinas";

	static ServidorDeTeste api;

	@BeforeAll
	static void subir() throws Exception {
		api = new ServidorDeTeste();
	}

	@AfterAll
	static void parar() {
		api.close();
	}

	private static String json(String nome, String data, String lote, String local) {
		return """
				{"nome":"%s","dataAplicacao":"%s","lote":"%s","localAplicacao":"%s"}""".formatted(nome, data, lote, local);
	}

	private Resp criar(String token, String nome, String data) throws Exception {
		return api.post(ROTA, json(nome, data, "L123", "UBS Centro"), token);
	}

	@Test
	void criarRetorna201ComIdEDadosSalvos() throws Exception {
		String token = api.cadastrarELogar();

		Resp r = criar(token, "  Gripe ", "2025-04-10");

		assertEquals(201, r.status());
		assertEquals("Gripe", r.texto("nome"));
		assertEquals("2025-04-10", r.texto("dataAplicacao"));
		assertEquals("L123", r.texto("lote"));
		assertEquals("UBS Centro", r.texto("localAplicacao"));
		assertNotNull(r.texto("id"));
	}

	@Test
	void loteELocalSaoOpcionais() throws Exception {
		String token = api.cadastrarELogar();

		Resp r = api.post(ROTA, "{\"nome\":\"Tétano\",\"dataAplicacao\":\"2024-01-02\"}", token);

		assertEquals(201, r.status());
		assertNull(r.texto("lote"));
		assertNull(r.texto("localAplicacao"));
	}

	@Test
	void listarMostraAsMaisRecentesPrimeiroESoDoUsuario() throws Exception {
		String token = api.cadastrarELogar();
		String outro = api.cadastrarELogar();
		criar(token, "Antiga", "2020-01-01");
		criar(token, "Recente", "2025-06-01");
		criar(token, "Meio", "2023-03-03");
		criar(outro, "Do outro", "2025-01-01");

		Resp r = api.get(ROTA, token);

		assertEquals(200, r.status());
		assertEquals(3, r.lista().size());
		assertEquals("Recente", r.lista().get(0).getAsJsonObject().get("nome").getAsString());
		assertEquals("Meio", r.lista().get(1).getAsJsonObject().get("nome").getAsString());
		assertEquals("Antiga", r.lista().get(2).getAsJsonObject().get("nome").getAsString());
	}

	@Test
	void listaVaziaQuandoNaoHaVacinas() throws Exception {
		Resp r = api.get(ROTA, api.cadastrarELogar());

		assertEquals(200, r.status());
		assertEquals(0, r.lista().size());
	}

	@Test
	void consultarAtualizarEExcluir() throws Exception {
		String token = api.cadastrarELogar();
		String id = criar(token, "Hepatite B", "2022-05-05").texto("id");
		String rotaId = ROTA + "/" + id;

		assertEquals("Hepatite B", api.get(rotaId, token).texto("nome"));

		Resp atualizada = api.put(rotaId, json("Hepatite B (2ª dose)", "2022-06-05", "", ""), token);
		assertEquals(200, atualizada.status());
		assertEquals("Hepatite B (2ª dose)", atualizada.texto("nome"));
		assertEquals("2022-06-05", atualizada.texto("dataAplicacao"));
		assertNull(atualizada.texto("lote"));
		assertEquals("2022-06-05", api.get(rotaId, token).texto("dataAplicacao"));

		assertEquals(204, api.delete(rotaId, token).status());
		assertEquals(404, api.get(rotaId, token).status());
		assertEquals(404, api.delete(rotaId, token).status());
	}

	@Test
	void usuarioNaoAcessaVacinaDeOutro() throws Exception {
		String tokenA = api.cadastrarELogar();
		String tokenB = api.cadastrarELogar();
		String id = criar(tokenA, "Da A", "2023-01-01").texto("id");
		String rotaId = ROTA + "/" + id;

		Resp ler = api.get(rotaId, tokenB);
		assertEquals(404, ler.status());
		assertEquals("VACINA_NAO_ENCONTRADA", ler.texto("codigo"));
		assertEquals(404, api.put(rotaId, json("Invadida", "2023-01-01", "", ""), tokenB).status());
		assertEquals(404, api.delete(rotaId, tokenB).status());

		Resp intacta = api.get(rotaId, tokenA);
		assertEquals(200, intacta.status());
		assertEquals("Da A", intacta.texto("nome"));
	}

	@Test
	void camposInvalidosRetornamErroPorCampo() throws Exception {
		String token = api.cadastrarELogar();

		Resp vazio = api.post(ROTA, "{}", token);
		assertEquals(400, vazio.status());
		assertEquals("VALIDACAO", vazio.texto("codigo"));
		assertEquals("Informe o nome da vacina.", vazio.texto("erros.nome"));
		assertEquals("Informe a data de aplicação.", vazio.texto("erros.dataAplicacao"));

		String amanha = LocalDate.now().plusDays(1).toString();
		assertEquals("A data de aplicação não pode ser futura.",
				criar(token, "Gripe", amanha).texto("erros.dataAplicacao"));
		assertEquals("Data inválida. Use o formato aaaa-mm-dd.",
				criar(token, "Gripe", "10/04/2025").texto("erros.dataAplicacao"));
		assertNotNull(criar(token, "a".repeat(151), "2025-01-01").texto("erros.nome"));
		assertNotNull(api.post(ROTA, json("Gripe", "2025-01-01", "l".repeat(101), ""), token).texto("erros.lote"));
		assertNotNull(api.post(ROTA, json("Gripe", "2025-01-01", "", "x".repeat(201)), token)
				.texto("erros.localAplicacao"));
	}

	@Test
	void atualizarComDadosInvalidosNaoAlteraAVacina() throws Exception {
		String token = api.cadastrarELogar();
		String id = criar(token, "Gripe", "2025-04-10").texto("id");

		assertEquals(400, api.put(ROTA + "/" + id, json("", "2025-04-10", "", ""), token).status());

		assertEquals("Gripe", api.get(ROTA + "/" + id, token).texto("nome"));
	}

	@Test
	void semTokenRetorna401() throws Exception {
		assertEquals(401, api.get(ROTA, null).status());
		assertEquals(401, api.post(ROTA, "{}", null).status());
		assertEquals(401, api.get(ROTA + "/1", null).status());
		assertEquals(401, api.delete(ROTA + "/1", null).status());
	}

	@Test
	void idInvalidoOuInexistenteRetorna404() throws Exception {
		String token = api.cadastrarELogar();

		assertEquals(404, api.get(ROTA + "/abc", token).status());
		assertEquals(404, api.get(ROTA + "/999999", token).status());
		assertEquals(404, api.get(ROTA + "/", token).status());
	}

	@Test
	void excluirAContaApagaAsVacinas() throws Exception {
		String email = api.emailUnico();
		api.cadastrar("Ana", email, ServidorDeTeste.SENHA, null);
		String token = api.login(email, ServidorDeTeste.SENHA).texto("token");
		criar(token, "Gripe", "2025-04-10");

		assertEquals(204, api.delete("/api/usuarios/me", token).status());

		assertEquals(0, api.contar("SELECT COUNT(*) FROM vacina v LEFT JOIN usuario u "
				+ "ON u.id_usuario = v.id_usuario WHERE u.id_usuario IS NULL"));
	}
}
