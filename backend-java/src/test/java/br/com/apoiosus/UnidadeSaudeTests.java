package br.com.apoiosus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import br.com.apoiosus.CnesFalso.Resposta;
import br.com.apoiosus.ServidorDeTeste.Resp;
import br.com.apoiosus.integration.CnesClient;

class UnidadeSaudeTests {

	static final String MUNICIPIOS = "/api/unidades-saude/municipios";
	static final String UPAS = "/api/unidades-saude/upas";
	static final String SANTOS = "354850";

	@Test
	void listaOsMunicipiosDoEstadoDeSaoPaulo() throws Exception {
		try (var cnes = new CnesFalso(i -> CnesFalso.semResultado()); var api = new ServidorDeTeste(cnes.cliente())) {
			Resp r = api.get(MUNICIPIOS, api.cadastrarELogar());

			assertEquals(200, r.status());
			assertEquals(645, r.lista().size());
			assertEquals("Adamantina", r.lista().get(0).getAsJsonObject().get("nome").getAsString());
			assertTrue(r.corpo().contains("{\"codigo\":\"355030\",\"nome\":\"São Paulo\"}"));
			assertTrue(r.corpo().contains("{\"codigo\":\"354850\",\"nome\":\"Santos\"}"));
			assertTrue(cnes.requisicoes.isEmpty(), "listar municípios não deve consultar o CNES");
		}
	}

	@Test
	void devolveSoUnidadesDoSusSemDadosDoDiretor() throws Exception {
		try (var cnes = new CnesFalso(i -> CnesFalso.pagina(CnesFalso.estabelecimento("UPA CENTRO", true),
				CnesFalso.estabelecimento("PRONTO SOCORRO PARTICULAR", false)));
				var api = new ServidorDeTeste(cnes.cliente())) {
			Resp r = api.get(UPAS + "?municipio=" + SANTOS, api.cadastrarELogar());

			assertEquals(200, r.status());
			assertEquals(1, r.json().getAsJsonArray("itens").size());
			var u = r.json().getAsJsonArray("itens").get(0).getAsJsonObject();
			assertEquals("UPA CENTRO", u.get("nome").getAsString());
			assertEquals("PRONTO ATENDIMENTO", u.get("tipo").getAsString());
			assertEquals("RUA DAS FLORES", u.get("logradouro").getAsString());
			assertEquals("100", u.get("numero").getAsString());
			assertEquals("CENTRO", u.get("bairro").getAsString());
			assertEquals("11010010", u.get("cep").getAsString());
			assertEquals("SANTOS", u.get("municipio").getAsString());
			assertEquals("SP", u.get("uf").getAsString());
			assertEquals("(13) 3222-1111", u.get("telefones").getAsJsonArray().get(0).getAsString());
			assertEquals("(13) 99123-4567", u.get("telefones").getAsJsonArray().get(1).getAsString());
			assertEquals("upa@exemplo.gov.br", u.get("email").getAsString());
			assertTrue(u.get("atendeSus").getAsBoolean());
			assertEquals("2026-10-05", u.get("atualizadoEm").getAsString());
			assertNull(r.texto("proximo"), "página incompleta: a fonte acabou");

			assertFalse(r.corpo().contains("12345678909"), "CPF do diretor não pode vazar");
			assertFalse(r.corpo().contains("DIRETOR"), "nome do diretor não pode vazar");
		}
	}

	@Test
	void enviaAoCnesOMunicipioOTipoEAAutenticacao() throws Exception {
		try (var cnes = new CnesFalso(i -> CnesFalso.semResultado()); var api = new ServidorDeTeste(cnes.cliente())) {
			api.get(UPAS + "?municipio=" + SANTOS, api.cadastrarELogar());

			assertEquals(1, cnes.requisicoes.size());
			String corpo = cnes.requisicoes.get(0);
			assertTrue(corpo.contains("<m:codigoIBGE>354850</m:codigoIBGE>"));
			assertTrue(corpo.contains("<tu:codigo>73</tu:codigo>"));
			assertTrue(corpo.contains("<wsse:Username>usuario</wsse:Username>"));
			assertTrue(corpo.contains(">senha</wsse:Password>"));
			assertTrue(corpo.contains("registroInicial>1<"));
			assertTrue(corpo.contains("quantidadeRegistros>20<"));
			// Elementos locais do filtro não levam namespace.
			assertTrue(corpo.contains("<localidade>"));
		}
	}

	@Test
	void municipioSemUnidadesDevolveListaVaziaEnaoErro() throws Exception {
		try (var cnes = new CnesFalso(i -> CnesFalso.semResultado()); var api = new ServidorDeTeste(cnes.cliente())) {
			Resp r = api.get(UPAS + "?municipio=" + SANTOS, api.cadastrarELogar());

			assertEquals(200, r.status());
			assertEquals(0, r.json().getAsJsonArray("itens").size());
			assertNull(r.texto("proximo"));
		}
	}

	@Test
	void paginaCheiaDeUnidadesDoSusDevolveProximo() throws Exception {
		try (var cnes = new CnesFalso(i -> CnesFalso.paginaCheia("UPA " + i + "-", 20));
				var api = new ServidorDeTeste(cnes.cliente())) {
			String token = api.cadastrarELogar();

			Resp primeira = api.get(UPAS + "?municipio=" + SANTOS, token);
			assertEquals(20, primeira.json().getAsJsonArray("itens").size());
			assertEquals("21", primeira.texto("proximo"));
			assertEquals(1, cnes.requisicoes.size());

			Resp segunda = api.get(UPAS + "?municipio=" + SANTOS + "&inicio=21", token);
			assertEquals("UPA 21- 1",
					segunda.json().getAsJsonArray("itens").get(0).getAsJsonObject().get("nome").getAsString());
			assertEquals("41", segunda.texto("proximo"));
			assertTrue(cnes.requisicoes.get(1).contains("registroInicial>21<"));
		}
	}

	@Test
	void juntaPaginasDoCnesAteTerUnidadesSuficientes() throws Exception {
		// Páginas 1 e 2 têm só 5 do SUS cada; a 3ª é incompleta (fim da fonte) com 1 do SUS.
		try (var cnes = new CnesFalso(i -> i < 41 ? CnesFalso.paginaCheia("P" + i + "-", 5)
				: CnesFalso.pagina(CnesFalso.estabelecimento("ULTIMA", true)));
				var api = new ServidorDeTeste(cnes.cliente())) {
			Resp r = api.get(UPAS + "?municipio=" + SANTOS, api.cadastrarELogar());

			assertEquals(11, r.json().getAsJsonArray("itens").size());
			assertNull(r.texto("proximo"));
			assertEquals(3, cnes.requisicoes.size());
		}
	}

	@Test
	void limitaQuantasPaginasDoCnesLeEmUmaChamada() throws Exception {
		// Nenhuma unidade do SUS e fonte sempre com página cheia: pára no teto e oferece continuar.
		try (var cnes = new CnesFalso(i -> CnesFalso.paginaCheia("PRIVADA " + i + "-", 0));
				var api = new ServidorDeTeste(cnes.cliente())) {
			Resp r = api.get(UPAS + "?municipio=" + SANTOS, api.cadastrarELogar());

			assertEquals(200, r.status());
			assertEquals(0, r.json().getAsJsonArray("itens").size());
			assertEquals("101", r.texto("proximo"));
			assertEquals(5, cnes.requisicoes.size());
		}
	}

	@Test
	void falhaDoCnesRetorna502EnaoListaVazia() throws Exception {
		try (var cnes = new CnesFalso(i -> CnesFalso.erroInterno()); var api = new ServidorDeTeste(cnes.cliente())) {
			Resp r = api.get(UPAS + "?municipio=" + SANTOS, api.cadastrarELogar());

			assertEquals(502, r.status());
			assertEquals("FONTE_INDISPONIVEL", r.texto("codigo"));
		}
	}

	@Test
	void respostaIlegivelDoCnesRetorna502() throws Exception {
		try (var cnes = new CnesFalso(i -> new Resposta(200, "isto não é XML"));
				var api = new ServidorDeTeste(cnes.cliente())) {
			assertEquals(502, api.get(UPAS + "?municipio=" + SANTOS, api.cadastrarELogar()).status());
		}
		try (var cnes = new CnesFalso(i -> new Resposta(200, "<a><b/></a>"));
				var api = new ServidorDeTeste(cnes.cliente())) {
			assertEquals(502, api.get(UPAS + "?municipio=" + SANTOS, api.cadastrarELogar()).status(),
					"XML válido, mas sem a resposta esperada, não pode virar lista vazia");
		}
	}

	@Test
	void cnesForaDoArRetorna502() throws Exception {
		// Porta fechada: sobe e derruba um servidor falso só para obter o endereço.
		String urlFechada;
		try (var cnes = new CnesFalso(i -> CnesFalso.semResultado())) {
			urlFechada = cnes.url();
		}
		var cliente = new CnesClient(urlFechada, "u", "s");
		try (var api = new ServidorDeTeste(cliente)) {
			Resp r = api.get(UPAS + "?municipio=" + SANTOS, api.cadastrarELogar());

			assertEquals(502, r.status());
			assertEquals("FONTE_INDISPONIVEL", r.texto("codigo"));
		}
	}

	@Test
	void municipioEInicioInvalidosRetornam400SemConsultarOCnes() throws Exception {
		try (var cnes = new CnesFalso(i -> CnesFalso.semResultado()); var api = new ServidorDeTeste(cnes.cliente())) {
			String token = api.cadastrarELogar();

			Resp ausente = api.get(UPAS, token);
			assertEquals(400, ausente.status());
			assertEquals("VALIDACAO", ausente.texto("codigo"));
			assertTrue(ausente.texto("erros.municipio").startsWith("Município inválido"));

			// 355030 existe em SP; 330455 é o Rio de Janeiro (outro estado).
			assertEquals(400, api.get(UPAS + "?municipio=330455", token).status());
			assertEquals(400, api.get(UPAS + "?municipio=abc", token).status());

			assertEquals("Informe um número inteiro maior que zero.",
					api.get(UPAS + "?municipio=" + SANTOS + "&inicio=0", token).texto("erros.inicio"));
			assertEquals(400, api.get(UPAS + "?municipio=" + SANTOS + "&inicio=abc", token).status());

			assertTrue(cnes.requisicoes.isEmpty());
		}
	}

	@Test
	void semTokenRetorna401() throws Exception {
		try (var cnes = new CnesFalso(i -> CnesFalso.semResultado()); var api = new ServidorDeTeste(cnes.cliente())) {
			assertEquals(401, api.get(MUNICIPIOS, null).status());
			assertEquals(401, api.get(UPAS + "?municipio=" + SANTOS, null).status());
			assertTrue(cnes.requisicoes.isEmpty());
		}
	}

	@Test
	void soAceitaGet() throws Exception {
		try (var cnes = new CnesFalso(i -> CnesFalso.semResultado()); var api = new ServidorDeTeste(cnes.cliente())) {
			String token = api.cadastrarELogar();

			assertEquals(405, api.post(UPAS, "{}", token).status());
			assertEquals(405, api.delete(MUNICIPIOS, token).status());
		}
	}
}
