package br.com.apoiosus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import br.com.apoiosus.ServidorDeTeste.Resp;

class PerfilSaudeTests {

	static final String ROTA = "/api/perfil";

	static ServidorDeTeste api;

	@BeforeAll
	static void subir() throws Exception {
		api = new ServidorDeTeste();
	}

	@AfterAll
	static void parar() {
		api.close();
	}

	private Resp salvar(String token, String json) throws Exception {
		return api.put(ROTA, json, token);
	}

	@Test
	void perfilNasceVazioNoCadastro() throws Exception {
		String token = api.cadastrarELogar();

		Resp r = api.get(ROTA, token);

		assertEquals(200, r.status());
		assertNotNull(r.texto("id"));
		assertNotNull(r.texto("usuarioId"));
		assertNull(r.texto("peso"));
		assertNull(r.texto("altura"));
		assertNull(r.texto("tipoSanguineo"));
		assertNull(r.texto("alergias"));
		assertNull(r.texto("observacoesMedicas"));
	}

	@Test
	void atualizarSalvaOsDadosENormalizaOTipoSanguineo() throws Exception {
		String token = api.cadastrarELogar();

		Resp r = salvar(token, """
				{"peso":72.5,"altura":1.75,"tipoSanguineo":" ab+ ","alergias":"Dipirona","observacoesMedicas":"Nenhuma"}""");

		assertEquals(200, r.status());
		assertEquals("72.5", r.texto("peso"));
		assertEquals("1.75", r.texto("altura"));
		assertEquals("AB+", r.texto("tipoSanguineo"));

		Resp depois = api.get(ROTA, token);
		assertEquals("72.5", depois.texto("peso"));
		assertEquals("Dipirona", depois.texto("alergias"));
		assertEquals("Nenhuma", depois.texto("observacoesMedicas"));
	}

	@Test
	void atualizarSubstituiTudo_oQueNaoForEnviadoFicaVazio() throws Exception {
		String token = api.cadastrarELogar();
		salvar(token, "{\"peso\":80,\"alergias\":\"Pólen\"}");

		Resp r = salvar(token, "{\"altura\":1.80}");

		assertNull(r.texto("peso"));
		assertNull(r.texto("alergias"));
		assertEquals("1.8", r.texto("altura"));
	}

	@Test
	void valoresInvalidosRetornamErroPorCampo() throws Exception {
		String token = api.cadastrarELogar();

		Resp r = salvar(token, "{\"peso\":0,\"altura\":-1,\"tipoSanguineo\":\"X+\"}");
		assertEquals(400, r.status());
		assertEquals("VALIDACAO", r.texto("codigo"));
		assertNotNull(r.texto("erros.peso"));
		assertNotNull(r.texto("erros.altura"));
		assertNotNull(r.texto("erros.tipoSanguineo"));

		assertNotNull(salvar(token, "{\"peso\":1001}").texto("erros.peso"));
		assertNotNull(salvar(token, "{\"altura\":3.5}").texto("erros.altura"));
		assertEquals(200, salvar(token, "{\"peso\":600,\"tipoSanguineo\":\"AB\"}").status(),
				"peso até 1000 kg e tipo sem sinal são aceitos, como no backend Spring");
		assertNotNull(salvar(token, "{\"alergias\":\"" + "a".repeat(5001) + "\"}").texto("erros.alergias"));
		assertNotNull(salvar(token, "{\"observacoesMedicas\":\"" + "a".repeat(5001) + "\"}")
				.texto("erros.observacoesMedicas"));
	}

	@Test
	void limparZeraOsCamposEMantemOPerfil() throws Exception {
		String email = api.emailUnico();
		api.cadastrar("Ana", email, ServidorDeTeste.SENHA, null);
		String token = api.login(email, ServidorDeTeste.SENHA).texto("token");
		salvar(token, "{\"peso\":60,\"tipoSanguineo\":\"O-\"}");

		assertEquals(204, api.delete(ROTA, token).status());

		Resp r = api.get(ROTA, token);
		assertEquals(200, r.status());
		assertNull(r.texto("peso"));
		assertNull(r.texto("tipoSanguineo"));
		assertEquals(1, api.contar("SELECT COUNT(*) FROM perfil_saude p JOIN usuario u "
				+ "ON u.id_usuario = p.id_usuario WHERE u.email = ?", email));
	}

	@Test
	void cadaUsuarioSoVeEAlteraOProprioPerfil() throws Exception {
		String tokenA = api.cadastrarELogar();
		String tokenB = api.cadastrarELogar();

		salvar(tokenA, "{\"peso\":90,\"alergias\":\"Só da A\"}");

		Resp doB = api.get(ROTA, tokenB);
		assertNull(doB.texto("peso"));
		assertNull(doB.texto("alergias"));
		assertEquals("Só da A", api.get(ROTA, tokenA).texto("alergias"));
	}

	@Test
	void semTokenRetorna401() throws Exception {
		assertEquals(401, api.get(ROTA, null).status());
		assertEquals(401, api.put(ROTA, "{}", null).status());
		assertEquals(401, api.delete(ROTA, null).status());
	}

	@Test
	void corpoAusenteRetorna400() throws Exception {
		String token = api.cadastrarELogar();
		assertEquals(400, api.put(ROTA, null, token).status());
	}
}
