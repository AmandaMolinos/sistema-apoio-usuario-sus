package br.com.apoiosus;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

// Validade de 0 hora: a sessão já nasce expirada.
@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:sqlite:target/apoio_sus_test.db?foreign_keys=on",
		"app.sessao.horas-validade=0" })
@AutoConfigureMockMvc
@Transactional
class SessaoExpiradaTests {

	@Autowired
	MockMvc mvc;

	@Test
	void tokenDeSessaoExpiradaRetorna401() throws Exception {
		String email = "expira-" + UUID.randomUUID() + "@exemplo.com";
		mvc.perform(post("/api/v1/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"nome":"Ana","email":"%s","senha":"senha1234"}""".formatted(email)))
				.andExpect(status().isCreated());

		String corpo = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"%s","senha":"senha1234"}""".formatted(email)))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		String token = JsonPath.read(corpo, "$.token");

		mvc.perform(get("/api/v1/usuarios/me").header("Authorization", "Bearer " + token))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.codigo").value("NAO_AUTENTICADO"));
	}
}
