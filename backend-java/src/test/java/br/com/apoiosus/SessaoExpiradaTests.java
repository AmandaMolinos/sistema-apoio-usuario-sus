package br.com.apoiosus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SessaoExpiradaTests {

	// Validade de 0 hora: a sessão já nasce expirada.
	@Test
	void tokenDeSessaoExpiradaRetorna401() throws Exception {
		try (var api = new ServidorDeTeste(0)) {
			String token = api.cadastrarELogar();

			var r = api.get("/api/usuarios/me", token);

			assertEquals(401, r.status());
			assertEquals("NAO_AUTENTICADO", r.texto("codigo"));
		}
	}
}
