package br.com.apoiosus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;

import org.junit.jupiter.api.Test;

import br.com.apoiosus.model.Municipio;
import br.com.apoiosus.service.MunicipioService;

class MunicipioServiceTests {

	private final MunicipioService servico = new MunicipioService();

	@Test
	void temOs645MunicipiosDeSaoPauloSemRepeticao() {
		assertEquals(645, servico.listar().size());
		assertEquals(645, new HashSet<>(servico.listar().stream().map(Municipio::codigo).toList()).size());
		assertEquals(645, new HashSet<>(servico.listar().stream().map(Municipio::nome).toList()).size());
	}

	@Test
	void codigosTemSeisDigitosEComecamCom35() {
		for (Municipio m : servico.listar()) {
			assertTrue(m.codigo().matches("35\\d{4}"), "código inválido: " + m);
			assertTrue(!m.nome().isBlank());
		}
	}

	@Test
	void buscaPorCodigo() {
		assertEquals("São Paulo", servico.buscar("355030").nome());
		assertEquals("Santos", servico.buscar("354850").nome());
		assertEquals("Mogi das Cruzes", servico.buscar("353060").nome());
		assertNull(servico.buscar("330455"), "Rio de Janeiro não é de São Paulo");
		assertNull(servico.buscar(null));
		assertNull(servico.buscar(""));
	}
}
