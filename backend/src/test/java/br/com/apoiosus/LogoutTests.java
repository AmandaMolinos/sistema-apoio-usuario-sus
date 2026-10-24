package br.com.apoiosus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest(properties = "spring.datasource.url=jdbc:sqlite:target/apoio_sus_test.db?foreign_keys=on")
@AutoConfigureMockMvc
@Transactional
class LogoutTests {

	@Autowired
	MockMvc mvc;

	@Autowired
	JdbcTemplate jdbc;

	private String cadastrar() throws Exception {
		String email = "logout-" + UUID.randomUUID() + "@exemplo.com";
		mvc.perform(post("/api/v1/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"nome":"Rita","email":"%s","senha":"senha1234"}""".formatted(email)))
				.andExpect(status().isCreated());
		return email;
	}

	private String login(String email) throws Exception {
		String corpo = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"%s","senha":"senha1234"}""".formatted(email)))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		return JsonPath.read(corpo, "$.token");
	}

	@Test
	void logoutRevogaOTokenUsado() throws Exception {
		String token = login(cadastrar());

		mvc.perform(post("/api/v1/auth/logout").header("Authorization", "Bearer " + token))
				.andExpect(status().isNoContent());

		mvc.perform(get("/api/v1/usuarios/me").header("Authorization", "Bearer " + token))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.codigo").value("NAO_AUTENTICADO"));

		Integer revogadas = jdbc.queryForObject("SELECT COUNT(*) FROM sessao WHERE revogada_em IS NOT NULL", Integer.class);
		assertThat(revogadas).isEqualTo(1);
	}

	@Test
	void logoutNaoAfetaOutraSessaoDoMesmoUsuario() throws Exception {
		String email = cadastrar();
		String tokenA = login(email);
		String tokenB = login(email);

		mvc.perform(post("/api/v1/auth/logout").header("Authorization", "Bearer " + tokenA))
				.andExpect(status().isNoContent());

		mvc.perform(get("/api/v1/usuarios/me").header("Authorization", "Bearer " + tokenA))
				.andExpect(status().isUnauthorized());
		mvc.perform(get("/api/v1/usuarios/me").header("Authorization", "Bearer " + tokenB))
				.andExpect(status().isOk());
	}

	@Test
	void logoutSemTokenRetorna401() throws Exception {
		mvc.perform(post("/api/v1/auth/logout")).andExpect(status().isUnauthorized());
	}

	@Test
	void logoutRepetidoComTokenJaRevogadoRetorna401() throws Exception {
		String token = login(cadastrar());
		mvc.perform(post("/api/v1/auth/logout").header("Authorization", "Bearer " + token))
				.andExpect(status().isNoContent());
		mvc.perform(post("/api/v1/auth/logout").header("Authorization", "Bearer " + token))
				.andExpect(status().isUnauthorized());
	}
}
