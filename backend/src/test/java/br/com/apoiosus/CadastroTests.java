package br.com.apoiosus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "spring.datasource.url=jdbc:sqlite:target/apoio_sus_test.db?foreign_keys=on")
@AutoConfigureMockMvc
@Transactional
class CadastroTests {

	@Autowired
	MockMvc mvc;

	@Autowired
	JdbcTemplate jdbc;

	private ResultActions cadastrar(String nome, String email, String senha, String dataNascimento) throws Exception {
		String data = dataNascimento == null ? "null" : "\"" + dataNascimento + "\"";
		String json = """
				{"nome":"%s","email":"%s","senha":"%s","dataNascimento":%s}""".formatted(nome, email, senha, data);
		return mvc.perform(post("/api/v1/auth/cadastro").contentType(MediaType.APPLICATION_JSON).content(json));
	}

	private String emailUnico() {
		return "teste-" + UUID.randomUUID() + "@exemplo.com";
	}

	@Test
	void cadastroValidoCriaUsuarioEPerfilVazioSemExporSenha() throws Exception {
		String email = emailUnico();

		cadastrar("Maria Silva", email.toUpperCase(), "senha1234", "1990-05-20")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.email").value(email))
				.andExpect(jsonPath("$.senha").doesNotExist())
				.andExpect(jsonPath("$.senhaHash").doesNotExist());

		String hash = jdbc.queryForObject("SELECT senha FROM usuario WHERE email = ?", String.class, email);
		assertThat(hash).startsWith("$2").isNotEqualTo("senha1234");

		Integer perfis = jdbc.queryForObject(
				"SELECT COUNT(*) FROM perfil_saude p JOIN usuario u ON u.id_usuario = p.id_usuario WHERE u.email = ?",
				Integer.class, email);
		assertThat(perfis).isEqualTo(1);
	}

	@Test
	void emailDuplicadoIgnorandoMaiusculasRetorna409() throws Exception {
		String email = emailUnico();
		cadastrar("Maria", email, "senha1234", null).andExpect(status().isCreated());

		cadastrar("Outra Maria", email.toUpperCase(), "senha1234", null)
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.codigo").value("EMAIL_JA_CADASTRADO"));
	}

	@Test
	void camposInvalidosRetornamErroPorCampo() throws Exception {
		LocalDate amanha = LocalDate.now().plusDays(1);

		cadastrar("", "nao-e-email", "curta", amanha.toString())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo").value("VALIDACAO"))
				.andExpect(jsonPath("$.erros.nome").exists())
				.andExpect(jsonPath("$.erros.email").exists())
				.andExpect(jsonPath("$.erros.senha").exists())
				.andExpect(jsonPath("$.erros.dataNascimento").exists());
	}

	@Test
	void corpoAusenteRetorna400() throws Exception {
		mvc.perform(post("/api/v1/auth/cadastro").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo").value("REQUISICAO_INVALIDA"));
	}
}
