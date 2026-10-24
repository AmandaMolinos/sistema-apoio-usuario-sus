package br.com.apoiosus.service;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;

import br.com.apoiosus.dto.request.CadastroRequest;
import br.com.apoiosus.dto.request.LoginRequest;
import br.com.apoiosus.dto.response.LoginResponse;
import br.com.apoiosus.dto.response.UsuarioResponse;
import br.com.apoiosus.exception.ApiException;
import br.com.apoiosus.model.Sessao;
import br.com.apoiosus.model.Usuario;
import br.com.apoiosus.repository.SessaoRepository;
import br.com.apoiosus.repository.UsuarioRepository;

public class AuthService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioRepository usuarios;
    private final SessaoRepository sessoes;
    private final SenhaService senhas;
    private final long horasValidadeSessao;
    // Usado no login quando o e-mail não existe, para o tempo de resposta não revelar se a conta existe.
    private final String hashFicticio;

    public AuthService(UsuarioRepository usuarios, SessaoRepository sessoes, SenhaService senhas,
            long horasValidadeSessao) {
        this.usuarios = usuarios;
        this.sessoes = sessoes;
        this.senhas = senhas;
        this.horasValidadeSessao = horasValidadeSessao;
        this.hashFicticio = senhas.gerarHash("hash-ficticio");
    }

    public static String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public UsuarioResponse cadastrar(CadastroRequest req) throws SQLException {
        req.validar();
        String email = normalizarEmail(req.email());

        if (usuarios.existePorEmail(email)) {
            throw new ApiException(409, "EMAIL_JA_CADASTRADO", "Este e-mail já está cadastrado.");
        }

        String senhaHash = senhas.gerarHash(req.senha());
        Usuario usuario = usuarios.inserirComPerfil(req.nome().trim(), email, senhaHash, req.dataNascimentoOuNull());
        return UsuarioResponse.de(usuario);
    }

    public LoginResponse login(LoginRequest req) throws SQLException {
        req.validar();
        Usuario usuario = usuarios.buscarPorEmail(normalizarEmail(req.email()));

        String hash = usuario != null ? usuario.senhaHash() : hashFicticio;
        boolean senhaCorreta = senhas.confere(req.senha(), hash);
        if (usuario == null || !senhaCorreta) {
            // Mesma mensagem para e-mail inexistente e senha errada.
            throw new ApiException(401, "CREDENCIAIS_INVALIDAS", "E-mail ou senha incorretos.");
        }

        // Token aleatório entregue ao cliente; no banco guardamos só o hash dele.
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime expiraEm = agora.plusHours(horasValidadeSessao);
        sessoes.inserir(usuario.id(), SenhaService.sha256(token), agora, expiraEm);

        return new LoginResponse(token, "Bearer", horasValidadeSessao * 3600, expiraEm.toString(),
                UsuarioResponse.de(usuario));
    }

    // Encerra só a sessão do token usado; outros dispositivos continuam logados.
    public void logout(int sessaoId) throws SQLException {
        sessoes.revogar(sessaoId);
    }

    // Confere o token recebido. Sem token, inexistente, revogado ou expirado: erro 401.
    public Sessao exigirLogin(String token) throws SQLException {
        if (token == null) {
            throw ApiException.naoAutenticado();
        }
        Sessao sessao = sessoes.buscarPorTokenHash(SenhaService.sha256(token));
        if (sessao == null || !sessao.estaValida()) {
            throw ApiException.naoAutenticado();
        }
        return sessao;
    }
}
