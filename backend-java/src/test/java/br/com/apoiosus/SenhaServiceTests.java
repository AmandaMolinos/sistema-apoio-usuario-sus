package br.com.apoiosus;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import br.com.apoiosus.service.SenhaService;

class SenhaServiceTests {

	private final SenhaService senhas = new SenhaService();

	@Test
	void hashConfereComASenhaCorretaEFalhaComAErrada() {
		String hash = senhas.gerarHash("senha1234");

		assertTrue(senhas.confere("senha1234", hash));
		assertFalse(senhas.confere("senha12345", hash));
	}

	@Test
	void mesmoSenhaGeraHashesDiferentesPorCausaDoSalt() {
		assertNotEquals(senhas.gerarHash("senha1234"), senhas.gerarHash("senha1234"));
	}

	@Test
	void hashArmazenadoMalformadoNaoConfere() {
		assertFalse(senhas.confere("senha1234", "lixo"));
	}
}
