package br.com.apoiosus;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpServer;

import br.com.apoiosus.integration.CnesClient;

// Servidor que imita o SOA-CNES numa porta livre, para testar sem depender da rede.
// "resposta" recebe o registroInicial pedido e devolve o que o CNES responderia.
class CnesFalso implements AutoCloseable {

	record Resposta(int status, String corpo) {
	}

	private static final Pattern INICIO = Pattern.compile("registroInicial>(\\d+)<");

	// Corpos das requisições recebidas, para conferir o que a API enviou.
	final List<String> requisicoes = new CopyOnWriteArrayList<>();

	private final HttpServer servidor;

	CnesFalso(IntFunction<Resposta> resposta) throws IOException {
		servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		servidor.createContext("/", ex -> {
			String corpo = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
			requisicoes.add(corpo);
			Matcher m = INICIO.matcher(corpo);
			int inicio = m.find() ? Integer.parseInt(m.group(1)) : 1;
			Resposta r = resposta.apply(inicio);
			byte[] bytes = r.corpo().getBytes(StandardCharsets.UTF_8);
			ex.getResponseHeaders().set("Content-Type", "application/soap+xml; charset=utf-8");
			ex.sendResponseHeaders(r.status(), bytes.length);
			ex.getResponseBody().write(bytes);
			ex.close();
		});
		servidor.start();
	}

	CnesClient cliente() {
		return new CnesClient("http://localhost:" + servidor.getAddress().getPort() + "/", "usuario", "senha");
	}

	String url() {
		return "http://localhost:" + servidor.getAddress().getPort() + "/";
	}

	@Override
	public void close() {
		servidor.stop(0);
	}

	// --- XMLs no formato real do CNES (dados inventados) ---

	static String estabelecimento(String nome, boolean sus) {
		return """
				<ns5:DadosGeraisEstabelecimentoSaude xmlns:ns5="http://servicos.saude.gov.br/schema/cnes/v1r0/dadosgeraiscnes">\
				<ns3:CodigoCNES xmlns:ns3="urn:a"><ns3:codigo>1234567</ns3:codigo></ns3:CodigoCNES>\
				<ns5:nomeFantasia><ns7:Nome xmlns:ns7="urn:b">%s</ns7:Nome></ns5:nomeFantasia>\
				<ns8:Endereco xmlns:ns8="urn:c">\
				<ns8:nomeLogradouro>RUA DAS FLORES</ns8:nomeLogradouro><ns8:numero>100</ns8:numero>\
				<ns8:Bairro><ns10:descricaoBairro xmlns:ns10="urn:d">CENTRO</ns10:descricaoBairro></ns8:Bairro>\
				<ns8:CEP><ns11:numeroCEP xmlns:ns11="urn:e">11010010</ns11:numeroCEP></ns8:CEP>\
				<ns8:Municipio><ns12:codigoMunicipio xmlns:ns12="urn:f">354850</ns12:codigoMunicipio>\
				<ns12:nomeMunicipio xmlns:ns12="urn:f">SANTOS</ns12:nomeMunicipio>\
				<ns12:UF xmlns:ns12="urn:f"><ns13:siglaUF xmlns:ns13="urn:g">SP</ns13:siglaUF></ns12:UF></ns8:Municipio>\
				</ns8:Endereco>\
				<ns5:dataAtualizacao>2026-10-05</ns5:dataAtualizacao>\
				<ns15:Diretor xmlns:ns15="urn:h"><ns15:CPF><ns16:numeroCPF xmlns:ns16="urn:i">12345678909</ns16:numeroCPF></ns15:CPF>\
				<ns15:nome><ns17:Nome xmlns:ns17="urn:j">FULANO DIRETOR SECRETO</ns17:Nome></ns15:nome></ns15:Diretor>\
				<ns18:tipoUnidade xmlns:ns18="urn:k"><ns18:codigo>73</ns18:codigo><ns18:descricao>PRONTO ATENDIMENTO</ns18:descricao></ns18:tipoUnidade>\
				<ns5:Telefone><ns20:DDD xmlns:ns20="urn:l">13</ns20:DDD><ns20:numeroTelefone xmlns:ns20="urn:l">32221111</ns20:numeroTelefone></ns5:Telefone>\
				<ns5:Telefone><ns20:DDD xmlns:ns20="urn:l">13</ns20:DDD><ns20:numeroTelefone xmlns:ns20="urn:l">991234567</ns20:numeroTelefone></ns5:Telefone>\
				<ns5:Email><ns22:descricaoEmail xmlns:ns22="urn:m">upa@exemplo.gov.br</ns22:descricaoEmail></ns5:Email>\
				<ns5:perteceSistemaSUS>%s</ns5:perteceSistemaSUS>\
				</ns5:DadosGeraisEstabelecimentoSaude>"""
				.formatted(nome, sus);
	}

	// Envelope de sucesso com os estabelecimentos dados.
	static Resposta pagina(String... estabelecimentos) {
		return new Resposta(200, """
				<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope"><soap:Body>\
				<est:responsePesquisarEstabelecimentoSaude xmlns:est="http://servicos.saude.gov.br/cnes/v2r0/estabelecimentosaudeservice">\
				<res:ResultadoPesquisaEstabelecimentoSaude xmlns:res="urn:r"><res:Estabelecimentos>%s</res:Estabelecimentos>\
				</res:ResultadoPesquisaEstabelecimentoSaude>\
				</est:responsePesquisarEstabelecimentoSaude></soap:Body></soap:Envelope>"""
				.formatted(String.join("", estabelecimentos)));
	}

	// Uma página cheia (20 registros): "total" unidades, das quais as primeiras "doSus" são do SUS.
	static Resposta paginaCheia(String prefixo, int doSus) {
		List<String> itens = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			itens.add(estabelecimento(prefixo + " " + (i + 1), i < doSus));
		}
		return pagina(itens.toArray(new String[0]));
	}

	// A falha com que o CNES avisa que não achou nada (HTTP 500).
	static Resposta semResultado() {
		return new Resposta(500, """
				<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope"><soap:Body><soap:Fault>\
				<soap:Reason><soap:Text xml:lang="pt-BR">Ocorreu um erro.</soap:Text></soap:Reason>\
				<soap:Detail><msf:MSFalha xmlns:msf="urn:s"><msf:Mensagem><men:codigo xmlns:men="urn:t">NO_RESULT</men:codigo>\
				<men:descricao xmlns:men="urn:t">Nenhum resultado foi encontrado para a consulta solicitada.</men:descricao>\
				</msf:Mensagem></msf:MSFalha></soap:Detail></soap:Fault></soap:Body></soap:Envelope>""");
	}

	static Resposta erroInterno() {
		return new Resposta(500, """
				<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope"><soap:Body><soap:Fault>\
				<soap:Detail><men:codigo xmlns:men="urn:t">OSB_INTERNAL_ERROR</men:codigo></soap:Detail>\
				</soap:Fault></soap:Body></soap:Envelope>""");
	}
}
