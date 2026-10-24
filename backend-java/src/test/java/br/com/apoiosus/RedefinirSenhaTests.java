package br.com.apoiosus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import br.com.apoiosus.ServidorDeTeste.Resp;

class RedefinirSenhaTests {

	static ServidorDeTeste api;

	@BeforeAll
	static void subir() throws Exception {
		api = new ServidorDeTeste();
	}

	@AfterAll
	static void parar() {
		api.close();
	}

	private static final String NOVA_SENHA = "novaSenha99";

	private String cadastrarEPedirCodigo() throws Exception {
		String email = api.emailUnico();
		api.cadastrar("Ana", email, ServidorDeTeste.SENHA, null);
		api.post("/api/auth/recuperar-senha", "{\"email\":\"" + email + "\"}", null);
		return email;
	}

	private Resp redefinir(String codigo, String novaSenha) throws Exception {
		return api.post("/api/auth/redefinir-senha",
				"{\"codigo\":\"" + codigo + "\",\"novaSenha\":\"" + novaSenha + "\"}", null);
	}

	@Test
	void codigoValidoTrocaASenhaEEncerraAsSessoesAntigas() throws Exception {
		String email = cadastrarEPedirCodigo();
		String tokenAntigo = api.login(email, ServidorDeTeste.SENHA).texto("token");
		String codigo = api.codigosDeRecuperacao.get(email);

		Resp r = redefinir(codigo, NOVA_SENHA);

		assertEquals(200, r.status());
		assertEquals(401, api.get("/api/usuarios/me", tokenAntigo).status());
		assertEquals(401, api.login(email, ServidorDeTeste.SENHA).status());
		assertEquals(200, api.login(email, NOVA_SENHA).status());
	}

	@Test
	void codigoSoPodeSerUsadoUmaVez() throws Exception {
		String email = cadastrarEPedirCodigo();
		String codigo = api.codigosDeRecuperacao.get(email);
		assertEquals(200, redefinir(codigo, NOVA_SENHA).status());

		Resp segunda = redefinir(codigo, "outraSenha88");

		assertEquals(400, segunda.status());
		assertEquals("CODIGO_INVALIDO", segunda.texto("codigo"));
		assertEquals(200, api.login(email, NOVA_SENHA).status());
	}

	@Test
	void aceitaCodigoSemHifenEEmMinusculas() throws Exception {
		String email = cadastrarEPedirCodigo();
		String codigo = api.codigosDeRecuperacao.get(email).replace("-", "").toLowerCase();

		assertEquals(200, redefinir(codigo, NOVA_SENHA).status());
	}

	@Test
	void codigoInexistenteRetornaCodigoInvalido() throws Exception {
		Resp r = redefinir("AAAA-BBBB-CCCC", NOVA_SENHA);

		assertEquals(400, r.status());
		assertEquals("CODIGO_INVALIDO", r.texto("codigo"));
		assertEquals("Código inválido ou expirado.", r.texto("mensagem"));
	}

	@Test
	void novoPedidoInvalidaOCodigoAnterior() throws Exception {
		String email = cadastrarEPedirCodigo();
		String primeiro = api.codigosDeRecuperacao.get(email);
		api.post("/api/auth/recuperar-senha", "{\"email\":\"" + email + "\"}", null);
		String segundo = api.codigosDeRecuperacao.get(email);
		assertNotEquals(primeiro, segundo);

		assertEquals(400, redefinir(primeiro, NOVA_SENHA).status());
		assertEquals(200, redefinir(segundo, NOVA_SENHA).status());
	}

	@Test
	void camposInvalidosRetornamErroDeValidacao() throws Exception {
		Resp r = redefinir("", "curta");

		assertEquals(400, r.status());
		assertEquals("VALIDACAO", r.texto("codigo"));
		assertNotNull(r.texto("erros.codigo"));
		assertEquals("A senha deve ter entre 8 e 72 caracteres.", r.texto("erros.novaSenha"));
	}

	@Test
	void senhaInvalidaNaoGastaOCodigo() throws Exception {
		String email = cadastrarEPedirCodigo();
		String codigo = api.codigosDeRecuperacao.get(email);

		assertEquals(400, redefinir(codigo, "curta").status());

		assertEquals(200, redefinir(codigo, NOVA_SENHA).status());
	}

	@Test
	void codigoExpiradoRetornaCodigoInvalido() throws Exception {
		// Validade de 0 minuto: o código já nasce expirado.
		try (var apiExpira = new ServidorDeTeste(24, 0)) {
			String email = apiExpira.emailUnico();
			apiExpira.cadastrar("Ana", email, ServidorDeTeste.SENHA, null);
			apiExpira.post("/api/auth/recuperar-senha", "{\"email\":\"" + email + "\"}", null);
			String codigo = apiExpira.codigosDeRecuperacao.get(email);

			Resp r = apiExpira.post("/api/auth/redefinir-senha",
					"{\"codigo\":\"" + codigo + "\",\"novaSenha\":\"" + NOVA_SENHA + "\"}", null);

			assertEquals(400, r.status());
			assertEquals("CODIGO_INVALIDO", r.texto("codigo"));
			assertEquals(200, apiExpira.login(email, ServidorDeTeste.SENHA).status());
		}
	}
}
