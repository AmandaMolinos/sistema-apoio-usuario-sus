package br.com.apoiosus.service;

import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.apoiosus.dto.request.CadastroRequest;
import br.com.apoiosus.dto.request.LoginRequest;
import br.com.apoiosus.dto.response.LoginResponse;
import br.com.apoiosus.dto.response.UsuarioResponse;
import br.com.apoiosus.exception.ApiException;
import br.com.apoiosus.model.entity.PerfilSaude;
import br.com.apoiosus.model.entity.Usuario;
import br.com.apoiosus.repository.PerfilSaudeRepository;
import br.com.apoiosus.repository.UsuarioRepository;

@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PerfilSaudeRepository perfis;
    private final PasswordEncoder passwordEncoder;
    private final SessaoService sessaoService;
    // Hash usado quando o e-mail não existe, para o tempo de resposta não revelar se a conta existe.
    private final String hashFicticio;

    public AuthService(UsuarioRepository usuarios, PerfilSaudeRepository perfis, PasswordEncoder passwordEncoder,
            SessaoService sessaoService) {
        this.usuarios = usuarios;
        this.perfis = perfis;
        this.passwordEncoder = passwordEncoder;
        this.sessaoService = sessaoService;
        this.hashFicticio = passwordEncoder.encode("hash-ficticio");
    }

    // Revoga apenas a sessão do token usado; outros dispositivos continuam logados.
    public void logout(Integer sessaoId) {
        sessaoService.revogar(sessaoId);
    }

    public LoginResponse login(LoginRequest req) {
        Usuario usuario = usuarios.findByEmail(normalizarEmail(req.email())).orElse(null);
        String hash = usuario != null ? usuario.getSenhaHash() : hashFicticio;
        boolean senhaConfere = passwordEncoder.matches(req.senha(), hash);
        if (usuario == null || !senhaConfere) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "CREDENCIAIS_INVALIDAS", "E-mail ou senha incorretos.");
        }
        var sessao = sessaoService.criar(usuario.getId());
        return new LoginResponse(sessao.token(), sessao.expiraEm(), UsuarioResponse.de(usuario));
    }

    public static String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    // Cria o usuário e o perfil de saúde vazio na mesma transação.
    @Transactional
    public UsuarioResponse cadastrar(CadastroRequest req) {
        String email = normalizarEmail(req.email());
        if (usuarios.existsByEmail(email)) {
            throw emailJaCadastrado();
        }
        try {
            Usuario usuario = usuarios.saveAndFlush(
                    new Usuario(req.nome().trim(), email, passwordEncoder.encode(req.senha()), req.dataNascimento()));
            perfis.save(new PerfilSaude(usuario));
            return UsuarioResponse.de(usuario);
        } catch (DataIntegrityViolationException e) {
            // Cadastro simultâneo com o mesmo e-mail: a restrição UNIQUE do banco decide.
            throw emailJaCadastrado();
        }
    }

    private ApiException emailJaCadastrado() {
        return new ApiException(HttpStatus.CONFLICT, "EMAIL_JA_CADASTRADO", "Este e-mail já está cadastrado.");
    }
}
