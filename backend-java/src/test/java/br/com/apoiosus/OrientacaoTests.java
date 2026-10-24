package br.com.apoiosus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import br.com.apoiosus.ServidorDeTeste.Resp;

class OrientacaoTests {

	static final String ROTA = "/api/orientacoes";

	static final Set<String> CATEGORIAS = Set.of("Prevenção", "Doenças", "Hábitos");
	static final Set<String> ICONES = Set.of("gripe", "alimentacao", "atividade", "saude_mental", "vacina", "sono",
			"higiene", "generico");

	@Test
	void listaAsOrientacoesNoFormatoDaTela() throws Exception {
		try (var api = new ServidorDeTeste()) {
			Resp r = api.get(ROTA, api.cadastrarELogar());

			assertEquals(200, r.status());
			var itens = r.json().getAsJsonArray("itens");
			assertTrue(itens.size() >= 8);
			var primeiro = itens.get(0).getAsJsonObject();
			assertEquals(1, primeiro.get("id").getAsInt());
			assertEquals("Prevenção da gripe", primeiro.get("titulo").getAsString());
			assertEquals("Prevenção", primeiro.get("categoria").getAsString());
			assertEquals("gripe", primeiro.get("icone").getAsString());
		}
	}

	@Test
	void todasAsOrientacoesTemCamposValidos() throws Exception {
		try (var api = new ServidorDeTeste()) {
			var itens = api.get(ROTA, api.cadastrarELogar()).json().getAsJsonArray("itens");

			var categoriasVistas = new java.util.HashSet<String>();
			int id = 0;
			for (var e : itens) {
				var o = e.getAsJsonObject();
				assertEquals(++id, o.get("id").getAsInt(), "ids sequenciais");
				assertFalse(o.get("titulo").getAsString().isBlank());
				assertTrue(o.get("titulo").getAsString().length() <= 40, o.get("titulo").getAsString());
				assertFalse(o.get("resumo").getAsString().isBlank());
				assertTrue(o.get("resumo").getAsString().length() <= 140, o.get("titulo").getAsString());
				assertTrue(CATEGORIAS.contains(o.get("categoria").getAsString()), o.get("titulo").getAsString());
				assertTrue(ICONES.contains(o.get("icone").getAsString()), o.get("titulo").getAsString());
				categoriasVistas.add(o.get("categoria").getAsString());
			}
			assertEquals(CATEGORIAS, categoriasVistas, "cada categoria do filtro deve ter ao menos uma orientação");
		}
	}

	@Test
	void exigeLogin() throws Exception {
		try (var api = new ServidorDeTeste()) {
			assertEquals(401, api.get(ROTA, null).status());
			assertEquals(401, api.get(ROTA, "token-invalido").status());
		}
	}
}
