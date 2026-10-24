package br.com.apoiosus;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.datasource.url=jdbc:sqlite:target/apoio_sus_test.db?foreign_keys=on")
@AutoConfigureMockMvc
class PingControllerTests {

	@Autowired
	MockMvc mvc;

	@Test
	void pingEPublico() throws Exception {
		mvc.perform(get("/api/v1/ping"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ok"));
	}

	@Test
	void rotaProtegidaSemTokenRetorna401() throws Exception {
		mvc.perform(get("/api/v1/usuarios/me")).andExpect(status().isUnauthorized());
	}
}
