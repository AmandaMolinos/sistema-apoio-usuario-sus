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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest(properties = "spring.datasource.url=jdbc:sqlite:target/apoio_sus_test.db?foreign_keys=on")
@AutoConfigureMockMvc
@Transactional
class LoginTests {

	@Autowired
	MockMvc mvc;

	@Autowired
	JdbcTemplate jdbc;

	private static final String SENHA = "senha1234";

	private String cadastrarUsuario() throws Exception {
		String email = "login-" + UUID.randomUUID() + "@exemplo.com";
		mvc.perform(post("/api/v1/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"nome":"Joao","email":"%s","senha":"%s"}""".formatted(email, SENHA)))
				.andExpect(status().isCreated());
		return email;
	}

	private ResultActions login(String email, String senha) throws Exception {
		return mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"%s","senha":"%s"}""".formatted(email, senha)));
	}

	private String loginEPegarToken(String email) throws Exception {
		String corpo = login(email, SENHA).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		return JsonPath.read(corpo, "$.token");
	}

	@Test
	void loginValidoRetornaTokenQueDaAcessoAoUsuarioLogado() throws Exception {
		String email = cadastrarUsuario();

		login(email.toUpperCase(), SENHA)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.expiraEm").isNotEmpty())
				.andExpect(jsonPath("$.usuario.email").value(email));

		String token = loginEPegarToken(email);
		mvc.perform(get("/api/v1/usuarios/me").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(email));
	}

	@Test
	void bancoGuardaSomenteOHashDoToken() throws Exception {
		String email = cadastrarUsuario();
		String token = loginEPegarToken(email);

		Integer comTokenPuro = jdbc.queryForObject("SELECT COUNT(*) FROM sessao WHERE token_hash = ?", Integer.class, token);
		assertThat(comTokenPuro).isZero();
		Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM sessao WHERE length(token_hash) = 64", Integer.class);
		assertThat(total).isPositive();
	}

	@Test
	void senhaErradaEEmailInexistenteRetornamAMesmaMensagem() throws Exception {
		String email = cadastrarUsuario();

		login(email, "senha-errada")
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.codigo").value("CREDENCIAIS_INVALIDAS"))
				.andExpect(jsonPath("$.mensagem").value("E-mail ou senha incorretos."));

		login("nao-existe@exemplo.com", SENHA)
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.codigo").value("CREDENCIAIS_INVALIDAS"))
				.andExpect(jsonPath("$.mensagem").value("E-mail ou senha incorretos."));
	}

	@Test
	void camposVaziosRetornamErroDeValidacao() throws Exception {
		login("", "")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros.email").exists())
				.andExpect(jsonPath("$.erros.senha").exists());
	}

	@Test
	void semTokenOuComTokenInvalidoRetorna401() throws Exception {
		mvc.perform(get("/api/v1/usuarios/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.codigo").value("NAO_AUTENTICADO"));
		mvc.perform(get("/api/v1/usuarios/me").header("Authorization", "Bearer token-inventado"))
				.andExpect(status().isUnauthorized());
	}
}
