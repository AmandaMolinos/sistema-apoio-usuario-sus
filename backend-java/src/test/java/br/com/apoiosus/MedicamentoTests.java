package br.com.apoiosus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import br.com.apoiosus.ServidorDeTeste.Resp;

class MedicamentoTests {

	static final String ROTA = "/api/medicamentos";

	static ServidorDeTeste api;

	@BeforeAll
	static void subir() throws Exception {
		api = new ServidorDeTeste();
	}

	@AfterAll
	static void parar() {
		api.close();
	}

	private static String json(String nome, String dosagem, String frequencia, String inicio, String fim) {
		return """
				{"nome":"%s","dosagem":"%s","frequencia":"%s","dataInicio":"%s","dataFim":"%s"}"""
				.formatted(nome, dosagem, frequencia, inicio, fim);
	}

	private Resp criar(String token, String nome) throws Exception {
		return api.post(ROTA, json(nome, "500 mg", "8 em 8 horas", "2025-01-10", "2025-01-17"), token);
	}

	@Test
	void criarRetorna201ComIdEDadosSalvos() throws Exception {
		String token = api.cadastrarELogar();

		Resp r = criar(token, "  Dipirona ");

		assertEquals(201, r.status());
		assertEquals("Dipirona", r.texto("nome"));
		assertEquals("500 mg", r.texto("dosagem"));
		assertEquals("8 em 8 horas", r.texto("frequencia"));
		assertEquals("2025-01-10", r.texto("dataInicio"));
		assertEquals("2025-01-17", r.texto("dataFim"));
		assertNotNull(r.texto("id"));
	}

	@Test
	void nomeDosagemFrequenciaEDataInicioSaoObrigatorios() throws Exception {
		String token = api.cadastrarELogar();

		Resp r = api.post(ROTA, "{\"nome\":\"Losartana\"}", token);

		assertEquals(400, r.status());
		assertEquals("VALIDACAO", r.texto("codigo"));
		assertEquals("Informe a dosagem.", r.texto("erros.dosagem"));
		assertEquals("Informe a frequência.", r.texto("erros.frequencia"));
		assertEquals("Informe a data de início.", r.texto("erros.dataInicio"));
		assertNull(r.texto("erros.nome"));
	}

	@Test
	void dataFinalEOpcional() throws Exception {
		String token = api.cadastrarELogar();

		Resp r = api.post(ROTA, json("Losartana", "50 mg", "1 vez ao dia", "2025-03-01", ""), token);

		assertEquals(201, r.status());
		assertNull(r.texto("dataFim"));
	}

	@Test
	void listarMostraEmOrdemAlfabeticaESoDoUsuario() throws Exception {
		String token = api.cadastrarELogar();
		String outro = api.cadastrarELogar();
		criar(token, "Paracetamol");
		criar(token, "amoxicilina");
		criar(token, "Ibuprofeno");
		criar(outro, "Do outro");

		Resp r = api.get(ROTA, token);

		assertEquals(200, r.status());
		assertEquals(3, r.lista().size());
		assertEquals("amoxicilina", r.lista().get(0).getAsJsonObject().get("nome").getAsString());
		assertEquals("Ibuprofeno", r.lista().get(1).getAsJsonObject().get("nome").getAsString());
		assertEquals("Paracetamol", r.lista().get(2).getAsJsonObject().get("nome").getAsString());
	}

	@Test
	void listaVaziaQuandoNaoHaMedicamentos() throws Exception {
		Resp r = api.get(ROTA, api.cadastrarELogar());

		assertEquals(200, r.status());
		assertEquals(0, r.lista().size());
	}

	@Test
	void consultarAtualizarEExcluir() throws Exception {
		String token = api.cadastrarELogar();
		String id = criar(token, "Omeprazol").texto("id");
		String rotaId = ROTA + "/" + id;

		assertEquals("Omeprazol", api.get(rotaId, token).texto("nome"));

		Resp atualizado = api.put(rotaId, json("Omeprazol", "40 mg", "1 vez ao dia", "2025-02-01", ""), token);
		assertEquals(200, atualizado.status());
		assertEquals("40 mg", atualizado.texto("dosagem"));
		assertEquals("1 vez ao dia", atualizado.texto("frequencia"));
		assertNull(atualizado.texto("dataFim"));
		assertEquals("40 mg", api.get(rotaId, token).texto("dosagem"));

		assertEquals(204, api.delete(rotaId, token).status());
		assertEquals(404, api.get(rotaId, token).status());
		assertEquals(404, api.delete(rotaId, token).status());
	}

	@Test
	void usuarioNaoAcessaMedicamentoDeOutro() throws Exception {
		String tokenA = api.cadastrarELogar();
		String tokenB = api.cadastrarELogar();
		String id = criar(tokenA, "Da A").texto("id");
		String rotaId = ROTA + "/" + id;

		Resp ler = api.get(rotaId, tokenB);
		assertEquals(404, ler.status());
		assertEquals("MEDICAMENTO_NAO_ENCONTRADO", ler.texto("codigo"));
		assertEquals(404, api.put(rotaId, json("Invadido", "1 mg", "1x", "2025-01-01", ""), tokenB).status());
		assertEquals(404, api.delete(rotaId, tokenB).status());

		Resp intacto = api.get(rotaId, tokenA);
		assertEquals(200, intacto.status());
		assertEquals("Da A", intacto.texto("nome"));
	}

	@Test
	void camposInvalidosRetornamErroPorCampo() throws Exception {
		String token = api.cadastrarELogar();

		Resp vazio = api.post(ROTA, "{}", token);
		assertEquals(400, vazio.status());
		assertEquals("VALIDACAO", vazio.texto("codigo"));
		assertEquals("Informe o nome do medicamento.", vazio.texto("erros.nome"));

		assertNotNull(criar(token, "a".repeat(151)).texto("erros.nome"));
		assertNotNull(api.post(ROTA, json("X", "d".repeat(101), "", "", ""), token).texto("erros.dosagem"));
		assertNotNull(api.post(ROTA, json("X", "", "f".repeat(151), "", ""), token).texto("erros.frequencia"));
		assertEquals("Data inválida. Use o formato aaaa-mm-dd.",
				api.post(ROTA, json("X", "", "", "10/01/2025", ""), token).texto("erros.dataInicio"));
		assertEquals("Data inválida. Use o formato aaaa-mm-dd.",
				api.post(ROTA, json("X", "", "", "", "amanhã"), token).texto("erros.dataFim"));
	}

	@Test
	void periodoInvertidoEInvalidoEMesmoDiaEAceito() throws Exception {
		String token = api.cadastrarELogar();

		Resp invertido = api.post(ROTA, json("X", "1 mg", "1x", "2025-02-10", "2025-02-09"), token);
		assertEquals(400, invertido.status());
		assertEquals("A data final não pode ser anterior à data de início.", invertido.texto("erros.dataFim"));

		assertEquals(201, api.post(ROTA, json("X", "1 mg", "1x", "2025-02-10", "2025-02-10"), token).status());
	}

	@Test
	void atualizarComDadosInvalidosNaoAlteraOMedicamento() throws Exception {
		String token = api.cadastrarELogar();
		String id = criar(token, "Dipirona").texto("id");

		assertEquals(400, api.put(ROTA + "/" + id, json("", "1 mg", "1x", "2025-01-01", ""), token).status());

		assertEquals("Dipirona", api.get(ROTA + "/" + id, token).texto("nome"));
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
	void excluirAContaApagaOsMedicamentos() throws Exception {
		String email = api.emailUnico();
		api.cadastrar("Ana", email, ServidorDeTeste.SENHA, null);
		String token = api.login(email, ServidorDeTeste.SENHA).texto("token");
		criar(token, "Dipirona");

		assertEquals(204, api.delete("/api/usuarios/me", token).status());

		assertEquals(0, api.contar("SELECT COUNT(*) FROM medicamento m LEFT JOIN usuario u "
				+ "ON u.id_usuario = m.id_usuario WHERE u.id_usuario IS NULL"));
	}
}
