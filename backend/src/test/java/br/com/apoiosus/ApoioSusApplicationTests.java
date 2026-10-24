package br.com.apoiosus;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(properties = "spring.datasource.url=jdbc:sqlite:target/apoio_sus_test.db?foreign_keys=on")
class ApoioSusApplicationTests {

	@Autowired
	JdbcTemplate jdbc;

	@Test
	void migrationCriaAsTabelasPrincipais() {
		Integer tabelas = jdbc.queryForObject(
				"SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name IN "
						+ "('usuario','perfil_saude','vacina','medicamento','unidade_saude',"
						+ "'campanha_vacinacao','orientacao_saude','analise_sintoma')",
				Integer.class);
		assertThat(tabelas).isEqualTo(8);
	}

}
