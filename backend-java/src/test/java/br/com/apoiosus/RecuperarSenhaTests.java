package br.com.apoiosus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import br.com.apoiosus.ServidorDeTeste.Resp;

class RecuperarSenhaTests {

	static ServidorDeTeste api;

	@BeforeAll
	static void subir() throws Exception {
		api = new ServidorDeTeste();
	}

	@AfterAll
	static void parar() {
		api.close();
	}

	private Resp recuperar(String email) throws Exception {
		return api.post("/api/auth/recuperar-senha", "{\"email\":\"" + email + "\"}", null);
	}

	@Test
	void emailCadastradoGeraCodigoDeUsoUnicoGuardadoSoComoHash() throws Exception {
		String email = api.emailUnico();
		api.cadastrar("Ana", email, ServidorDeTeste.SENHA, null);

		Resp r = recuperar(email.toUpperCase());

		assertEquals(200, r.status());
		String codigo = api.codigosDeRecuperacao.get(email);
		assertNotNull(codigo);
		assertTrue(codigo.matches("[A-Z2-9]{4}-[A-Z2-9]{4}-[A-Z2-9]{4}"), codigo);
		assertEquals(1, api.contar("SELECT COUNT(*) FROM recuperacao_senha r JOIN usuario u "
				+ "ON u.id_usuario = r.id_usuario WHERE u.email = ? AND length(r.token_hash) = 64", email));
		assertEquals(0, api.contar("SELECT COUNT(*) FROM recuperacao_senha WHERE token_hash IN (?, ?)",
				codigo, codigo.replace("-", "")));
	}

	@Test
	void emailInexistenteRetornaAMesmaRespostaSemGerarCodigo() throws Exception {
		String email = api.emailUnico();
		api.cadastrar("Ana", email, ServidorDeTeste.SENHA, null);
		Resp existente = recuperar(email);

		String inexistente = api.emailUnico();
		Resp r = recuperar(inexistente);

		assertEquals(200, r.status());
		assertEquals(existente.corpo(), r.corpo());
		assertNull(api.codigosDeRecuperacao.get(inexistente));
	}

	@Test
	void novoPedidoInvalidaOCodigoAnterior() throws Exception {
		String email = api.emailUnico();
		api.cadastrar("Ana", email, ServidorDeTeste.SENHA, null);

		recuperar(email);
		String primeiro = api.codigosDeRecuperacao.get(email);
		recuperar(email);
		String segundo = api.codigosDeRecuperacao.get(email);

		assertFalse(primeiro.equals(segundo));
		assertEquals(2, api.contar("SELECT COUNT(*) FROM recuperacao_senha r JOIN usuario u "
				+ "ON u.id_usuario = r.id_usuario WHERE u.email = ?", email));
		assertEquals(1, api.contar("SELECT COUNT(*) FROM recuperacao_senha r JOIN usuario u "
				+ "ON u.id_usuario = r.id_usuario WHERE u.email = ? AND r.usado_em IS NULL", email));
	}

	@Test
	void emailVazioOuInvalidoRetornaErroDeValidacao() throws Exception {
		Resp vazio = recuperar("");
		assertEquals(400, vazio.status());
		assertEquals("Informe o e-mail.", vazio.texto("erros.email"));

		Resp invalido = recuperar("nao-e-email");
		assertEquals(400, invalido.status());
		assertEquals("E-mail inválido.", invalido.texto("erros.email"));
	}

	@Test
	void corpoAusenteRetorna400() throws Exception {
		assertEquals(400, api.post("/api/auth/recuperar-senha", null, null).status());
	}
}
