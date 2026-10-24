package br.com.apoiosus;

import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

import br.com.apoiosus.controller.AuthController;
import br.com.apoiosus.controller.UnidadeSaudeController;
import br.com.apoiosus.integration.CnesClient;
import br.com.apoiosus.controller.OrientacaoController;
import br.com.apoiosus.controller.SintomaController;
import br.com.apoiosus.service.SintomaService;
import br.com.apoiosus.service.OrientacaoService;
import br.com.apoiosus.service.MunicipioService;
import br.com.apoiosus.service.UnidadeSaudeService;
import br.com.apoiosus.controller.PerfilSaudeController;
import br.com.apoiosus.controller.PingController;
import br.com.apoiosus.controller.MedicamentoController;
import br.com.apoiosus.controller.VacinaController;
import br.com.apoiosus.controller.UsuarioController;
import br.com.apoiosus.repository.PerfilSaudeRepository;
import br.com.apoiosus.repository.RecuperacaoSenhaRepository;
import br.com.apoiosus.repository.SessaoRepository;
import br.com.apoiosus.repository.UsuarioRepository;
import br.com.apoiosus.repository.MedicamentoRepository;
import br.com.apoiosus.repository.VacinaRepository;
import br.com.apoiosus.service.AuthService;
import br.com.apoiosus.service.PerfilSaudeService;
import br.com.apoiosus.service.RecuperacaoSenhaService;
import br.com.apoiosus.service.SenhaService;
import br.com.apoiosus.service.UsuarioService;
import br.com.apoiosus.service.MedicamentoService;
import br.com.apoiosus.service.VacinaService;

public class ApoioSusApplication {

    public static void main(String[] args) throws Exception {
        int porta = Integer.parseInt(variavel("API_PORT", "8080"));
        String dbUrl = comCredenciais(variavel("DB_URL", "jdbc:mysql://localhost:3306/apoio_sus?createDatabaseIfNotExist=true"));
        long horasSessao = Long.parseLong(variavel("SESSAO_HORAS_VALIDADE", "24"));
        long minutosRecuperacao = Long.parseLong(variavel("RECUPERACAO_MINUTOS_VALIDADE", "30"));

        CnesClient cnes = new CnesClient(variavel("CNES_URL", CnesClient.URL_PADRAO),
                variavel("CNES_USUARIO", CnesClient.USUARIO_PADRAO), variavel("CNES_SENHA", CnesClient.SENHA_PADRAO));

        iniciar(porta, dbUrl, horasSessao, minutosRecuperacao, cnes);
        System.out.println("Apoio SUS (Java puro) em http://localhost:" + porta);
    }

    public static HttpServer iniciar(int porta, String dbUrl, long horasSessao, long minutosRecuperacao)
            throws Exception {
        return iniciar(porta, dbUrl, horasSessao, minutosRecuperacao,
                new CnesClient(CnesClient.URL_PADRAO, CnesClient.USUARIO_PADRAO, CnesClient.SENHA_PADRAO));
    }

    // Cria cada objeto e entrega aos que precisam dele (sem framework de injeção), depois sobe o servidor.
    public static HttpServer iniciar(int porta, String dbUrl, long horasSessao, long minutosRecuperacao,
            CnesClient cnes) throws Exception {
        Banco banco = new Banco(dbUrl);
        banco.criarTabelas();

        UsuarioRepository usuarios = new UsuarioRepository(banco);
        SessaoRepository sessoes = new SessaoRepository(banco);
        RecuperacaoSenhaRepository recuperacoes = new RecuperacaoSenhaRepository(banco);

        SenhaService senhas = new SenhaService();
        AuthService authService = new AuthService(usuarios, sessoes, senhas, horasSessao);
        UsuarioService usuarioService = new UsuarioService(usuarios);
        PerfilSaudeService perfilService = new PerfilSaudeService(new PerfilSaudeRepository(banco));
        VacinaService vacinaService = new VacinaService(new VacinaRepository(banco));
        MedicamentoService medicamentoService = new MedicamentoService(new MedicamentoRepository(banco));
        MunicipioService municipioService = new MunicipioService();
        UnidadeSaudeService unidadeService = new UnidadeSaudeService(municipioService, cnes);
        OrientacaoService orientacaoService = new OrientacaoService();
        RecuperacaoSenhaService recuperacaoService =
                new RecuperacaoSenhaService(usuarios, recuperacoes, senhas, minutosRecuperacao);

        HttpServer servidor = HttpServer.create(new InetSocketAddress(porta), 0);
        new PingController().registrar(servidor);
        new AuthController(authService, recuperacaoService).registrar(servidor);
        new UsuarioController(authService, usuarioService).registrar(servidor);
        new PerfilSaudeController(authService, perfilService).registrar(servidor);
        new VacinaController(authService, vacinaService).registrar(servidor);
        new MedicamentoController(authService, medicamentoService).registrar(servidor);
        new UnidadeSaudeController(authService, municipioService, unidadeService).registrar(servidor);
        new OrientacaoController(authService, orientacaoService).registrar(servidor);
        new SintomaController(authService, new SintomaService()).registrar(servidor);
        Http.rotaNaoEncontrada(servidor);

        servidor.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        servidor.start();
        return servidor;
    }

    // Para MySQL: acrescenta DB_USUARIO e DB_SENHA à URL (assim a senha não precisa ficar escrita em DB_URL).
    private static String comCredenciais(String url) {
        String usuario = variavel("DB_USUARIO", "");
        String senha = variavel("DB_SENHA", "");
        if (!url.startsWith("jdbc:mysql:") || usuario.isEmpty()) {
            return url;
        }
        return url + (url.contains("?") ? "&" : "?") + "user=" + URLEncoder.encode(usuario, StandardCharsets.UTF_8)
                + "&password=" + URLEncoder.encode(senha, StandardCharsets.UTF_8);
    }

    private static String variavel(String nome, String padrao) {
        String valor = System.getenv(nome);
        return valor == null || valor.isBlank() ? padrao : valor;
    }
}
