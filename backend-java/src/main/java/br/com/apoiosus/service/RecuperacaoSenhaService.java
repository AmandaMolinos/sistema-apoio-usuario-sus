package br.com.apoiosus.service;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.logging.Logger;

import br.com.apoiosus.dto.request.RecuperarSenhaRequest;
import br.com.apoiosus.dto.request.RedefinirSenhaRequest;
import br.com.apoiosus.exception.ApiException;
import br.com.apoiosus.model.Usuario;
import br.com.apoiosus.repository.RecuperacaoSenhaRepository;
import br.com.apoiosus.repository.UsuarioRepository;

public class RecuperacaoSenhaService {

    private static final Logger LOG = Logger.getLogger(RecuperacaoSenhaService.class.getName());

    // Letras e números sem 0/O/1/I para não confundir na hora de digitar.
    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioRepository usuarios;
    private final RecuperacaoSenhaRepository recuperacoes;
    private final SenhaService senhas;
    private final long minutosValidade;

    public RecuperacaoSenhaService(UsuarioRepository usuarios, RecuperacaoSenhaRepository recuperacoes,
            SenhaService senhas, long minutosValidade) {
        this.usuarios = usuarios;
        this.recuperacoes = recuperacoes;
        this.senhas = senhas;
        this.minutosValidade = minutosValidade;
    }

    // Código inválido, expirado ou já usado dão a mesma resposta (não revela qual foi o caso).
    public void redefinir(RedefinirSenhaRequest req) throws SQLException {
        req.validar();
        String novaSenhaHash = senhas.gerarHash(req.novaSenha());
        boolean trocou = recuperacoes.redefinirSenha(hashDoCodigo(req.codigo()), novaSenhaHash);
        if (!trocou) {
            throw new ApiException(400, "CODIGO_INVALIDO", "Código inválido ou expirado.");
        }
    }

    // Não diz se o e-mail existe: para quem chama o resultado é sempre o mesmo.
    public void solicitar(RecuperarSenhaRequest req) throws SQLException {
        req.validar();
        Usuario usuario = usuarios.buscarPorEmail(AuthService.normalizarEmail(req.email()));
        if (usuario == null) {
            return;
        }

        String codigo = gerarCodigo();
        LocalDateTime expiraEm = LocalDateTime.now().plusMinutes(minutosValidade);
        recuperacoes.substituirCodigo(usuario.id(), hashDoCodigo(codigo), expiraEm);

        // Ambiente de estudo: em vez de enviar e-mail, o código é escrito no log do servidor.
        String formatado = codigo.substring(0, 4) + "-" + codigo.substring(4, 8) + "-" + codigo.substring(8);
        LOG.info("[RECUPERACAO DE SENHA] e-mail=" + usuario.email() + " codigo=" + formatado
                + " expira em " + expiraEm);
    }

    // Aceita o código com ou sem hífens e em maiúsculas ou minúsculas.
    static String hashDoCodigo(String codigo) {
        return SenhaService.sha256(codigo.replace("-", "").trim().toUpperCase());
    }

    private static String gerarCodigo() {
        StringBuilder codigo = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            codigo.append(ALFABETO.charAt(RANDOM.nextInt(ALFABETO.length())));
        }
        return codigo.toString();
    }
}
