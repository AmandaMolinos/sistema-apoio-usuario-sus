package br.com.apoiosus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import br.com.apoiosus.ServidorDeTeste.Resp;

class SintomaTests {

	static final String ROTA = "/api/sintomas/analise";

	private static Resp analisar(ServidorDeTeste api, String token, String texto) throws Exception {
		JsonObject corpo = new JsonObject();
		corpo.addProperty("texto", texto);
		return api.post(ROTA, corpo.toString(), token);
	}

	private static List<String> nomes(Resp r) {
		List<String> nomes = new ArrayList<>();
		for (var e : r.json().getAsJsonArray("sintomas")) {
			nomes.add(e.getAsJsonObject().get("nome").getAsString());
		}
		return nomes;
	}

	@Test
	void reconheceSintomasSemAcentoNemMaiusculas() throws Exception {
		try (var api = new ServidorDeTeste()) {
			String token = api.cadastrarELogar();

			Resp r = analisar(api, token, "Estou com FEBRE e dor de CABEÇA, tambem tosse...");

			assertEquals(200, r.status());
			assertEquals(List.of("Febre", "Tosse", "Dor de cabeça"), nomes(r));
			assertTrue(r.json().get("reconhecido").getAsBoolean());
			assertFalse(r.json().get("urgente").getAsBoolean());
			assertEquals(0, r.json().getAsJsonArray("alertas").size());
			assertTrue(r.json().get("aviso").getAsString().contains("não substitui"));

			var febre = r.json().getAsJsonArray("sintomas").get(0).getAsJsonObject();
			assertFalse(febre.getAsJsonArray("cuidados").isEmpty());
			assertFalse(febre.getAsJsonArray("procurar").isEmpty());
		}
	}

	@Test
	void sinalDeAlertaViraUrgenteMesmoSemOutrosSintomas() throws Exception {
		try (var api = new ServidorDeTeste()) {
			String token = api.cadastrarELogar();

			Resp r = analisar(api, token, "Sinto dor no peito e falta de ar");

			assertEquals(200, r.status());
			assertTrue(r.json().get("urgente").getAsBoolean());
			assertTrue(r.json().get("reconhecido").getAsBoolean());
			JsonArray alertas = r.json().getAsJsonArray("alertas");
			assertEquals(3, alertas.size(), "um aviso por sinal e a orientação final");
			assertTrue(alertas.get(alertas.size() - 1).getAsString().contains("192"));
		}
	}

	@Test
	void alertaConvivendoComSintomaComum() throws Exception {
		try (var api = new ServidorDeTeste()) {
			Resp r = analisar(api, api.cadastrarELogar(), "febre e desmaiei hoje cedo");

			assertTrue(r.json().get("urgente").getAsBoolean());
			assertEquals(List.of("Febre"), nomes(r));
		}
	}

	@Test
	void mencaoASuicidioIndicaCvvESamu() throws Exception {
		try (var api = new ServidorDeTeste()) {
			Resp r = analisar(api, api.cadastrarELogar(), "tenho pensamentos de suicídio");

			assertTrue(r.json().get("urgente").getAsBoolean());
			assertTrue(r.corpo().contains("188"));
			assertTrue(r.corpo().contains("192"));
		}
	}

	@Test
	void sintomaNegadoNaoEhConsiderado() throws Exception {
		try (var api = new ServidorDeTeste()) {
			String token = api.cadastrarELogar();

			assertEquals(List.of("Tosse"), nomes(analisar(api, token, "tenho tosse mas não tenho febre")));
			assertEquals(List.of("Tosse"), nomes(analisar(api, token, "sem febre, apenas tosse")));
			assertEquals(List.of(), nomes(analisar(api, token, "sem febre e sem tosse")));
		}
	}

	@Test
	void textoSemNadaConhecidoNaoInventaOrientacao() throws Exception {
		try (var api = new ServidorDeTeste()) {
			Resp r = analisar(api, api.cadastrarELogar(), "me sinto estranho desde ontem");

			assertEquals(200, r.status());
			assertFalse(r.json().get("reconhecido").getAsBoolean());
			assertFalse(r.json().get("urgente").getAsBoolean());
			assertEquals(0, r.json().getAsJsonArray("sintomas").size());
		}
	}

	@Test
	void naoConfundePalavraInteiraComPedaco() throws Exception {
		try (var api = new ServidorDeTeste()) {
			// "tonto" não pode casar dentro de "pontoou"; "febre" não casa em "febrento"
			Resp r = analisar(api, api.cadastrarELogar(), "fiquei pontoou febrento");

			assertFalse(r.json().get("reconhecido").getAsBoolean());
		}
	}

	@Test
	void validaOTexto() throws Exception {
		try (var api = new ServidorDeTeste()) {
			String token = api.cadastrarELogar();

			Resp vazio = analisar(api, token, "   ");
			assertEquals(400, vazio.status());
			assertEquals("VALIDACAO", vazio.texto("codigo"));
			assertTrue(vazio.texto("erros.texto").contains("Descreva"));

			Resp longo = analisar(api, token, "febre ".repeat(100));
			assertEquals(400, longo.status());
			assertTrue(longo.texto("erros.texto").contains("500"));

			assertEquals(400, api.post(ROTA, "{}", token).status());
			assertEquals(400, api.post(ROTA, "isto nao e json", token).status());
		}
	}

	@Test
	void naoGuardaOTextoDigitado() throws Exception {
		try (var api = new ServidorDeTeste()) {
			analisar(api, api.cadastrarELogar(), "febre muito secreta 12345");

			assertEquals(0, api.contar("SELECT COUNT(*) FROM perfil_saude WHERE observacoes_medicas LIKE '%secreta%'"
					+ " OR alergias LIKE '%secreta%'"));
		}
	}

	@Test
	void exigeLogin() throws Exception {
		try (var api = new ServidorDeTeste()) {
			assertEquals(401, api.post(ROTA, "{\"texto\":\"febre\"}", null).status());
			assertEquals(401, api.post(ROTA, "{\"texto\":\"febre\"}", "token-invalido").status());
		}
	}
}
