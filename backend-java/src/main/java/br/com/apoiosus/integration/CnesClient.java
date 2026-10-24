package br.com.apoiosus.integration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.helpers.DefaultHandler;

import br.com.apoiosus.exception.ApiException;
import br.com.apoiosus.model.Unidade;

// Cliente do SOA-CNES (Ministério da Saúde): serviço SOAP com credencial pública, descrito em
// docs/FONTES_PUBLICAS.md. Monta o XML à mão e lê a resposta com o parser do JDK.
public class CnesClient {

    public static final String URL_PADRAO = "https://servicos.saude.gov.br/cnes/EstabelecimentoSaudeService/v2r0";
    public static final String USUARIO_PADRAO = "CNES.PUBLICO";
    public static final String SENHA_PADRAO = "cnes#2015public";

    // O CNES entrega no máximo 20 registros por página.
    public static final int POR_PAGINA = 20;

    // Tipo de unidade 73 = pronto atendimento (UPA, AMA 24h etc.).
    private static final String TIPO_PRONTO_ATENDIMENTO = "73";

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final String url;
    private final String usuario;
    private final String senha;

    public CnesClient(String url, String usuario, String senha) {
        this.url = url;
        this.usuario = usuario;
        this.senha = senha;
    }

    // Uma página (até 20) de unidades de pronto atendimento do município, a partir do registro "inicio" (1, 21...).
    // Município sem nenhuma unidade devolve lista vazia. Qualquer outra falha vira 502 FONTE_INDISPONIVEL.
    // Atenção: a lista inclui unidades que não são do SUS; quem chama decide se filtra.
    public List<Unidade> pesquisarProntoAtendimento(String codigoMunicipio, int inicio) {
        HttpResponse<String> resposta;
        try {
            HttpRequest requisicao = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/soap+xml; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(montarEnvelope(codigoMunicipio, inicio),
                            StandardCharsets.UTF_8))
                    .build();
            resposta = http.send(requisicao, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException | IllegalArgumentException e) {
            throw fonteIndisponivel();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw fonteIndisponivel();
        }

        // O CNES responde "nenhum resultado" como falha SOAP com este código; para nós é só lista vazia.
        if (resposta.body().contains(">NO_RESULT<")) {
            return List.of();
        }
        if (resposta.statusCode() != 200) {
            throw fonteIndisponivel();
        }
        try {
            return lerUnidades(resposta.body());
        } catch (Exception e) {
            throw fonteIndisponivel();
        }
    }

    private String montarEnvelope(String codigoMunicipio, int inicio) {
        // Detalhes exigidos pelo serviço: "localidade" e "raio" não levam namespace, e a coordenada é
        // obrigatória mesmo com código de município (0,0 é aceito e não filtra nada).
        return """
                <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" \
                xmlns:tns="http://servicos.saude.gov.br/cnes/v2r0/estabelecimentosaudeservice" \
                xmlns:f="http://servicos.saude.gov.br/wsdl/mensageria/estabelecimentosaudeservice/v2r0/filtropesquisaestabelecimento.v1r0" \
                xmlns:l="http://servicos.saude.gov.br/schema/cnes/v1r0/localizacao" \
                xmlns:m="http://servicos.saude.gov.br/schema/corporativo/v1r3/municipio" \
                xmlns:lt="http://servicos.saude.gov.br/schema/cnes/v1r0/listatipounidade" \
                xmlns:tu="http://servicos.saude.gov.br/schema/cnes/v1r0/tipounidade" \
                xmlns:p="http://servicos.saude.gov.br/wsdl/mensageria/v1/paginacao">\
                <soap:Header>\
                <wsse:Security soap:mustUnderstand="true" xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">\
                <wsse:UsernameToken>\
                <wsse:Username>%s</wsse:Username>\
                <wsse:Password Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText">%s</wsse:Password>\
                </wsse:UsernameToken>\
                </wsse:Security>\
                </soap:Header>\
                <soap:Body>\
                <tns:requestPesquisarEstabelecimentoSaude>\
                <f:FiltroPesquisaEstabelecimentoSaude>\
                <localidade>\
                <l:Localizacao><l:longitude>0</l:longitude><l:latitude>0</l:latitude></l:Localizacao>\
                <m:codigoIBGE>%s</m:codigoIBGE>\
                </localidade>\
                <lt:ListaTipoUnidade><lt:tipoUnidade><tu:codigo>%s</tu:codigo></lt:tipoUnidade></lt:ListaTipoUnidade>\
                </f:FiltroPesquisaEstabelecimentoSaude>\
                <p:Paginacao><p:registroInicial>%d</p:registroInicial><p:quantidadeRegistros>%d</p:quantidadeRegistros></p:Paginacao>\
                </tns:requestPesquisarEstabelecimentoSaude>\
                </soap:Body>\
                </soap:Envelope>"""
                .formatted(escapar(usuario), escapar(senha), escapar(codigoMunicipio), TIPO_PRONTO_ATENDIMENTO, inicio,
                        POR_PAGINA);
    }

    private static List<Unidade> lerUnidades(String xml) throws Exception {
        DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
        fabrica.setNamespaceAware(true);
        fabrica.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        DocumentBuilder construtor = fabrica.newDocumentBuilder();
        construtor.setErrorHandler(new DefaultHandler()); // sem isto o parser imprime os erros no console
        Document documento = construtor.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        // Uma resposta sem a lista de estabelecimentos nem o elemento da resposta não é uma resposta válida.
        if (documento.getElementsByTagNameNS("*", "ResultadoPesquisaEstabelecimentoSaude").getLength() == 0
                && documento.getElementsByTagNameNS("*", "responsePesquisarEstabelecimentoSaude").getLength() == 0) {
            throw new IOException("Resposta inesperada do CNES.");
        }

        List<Unidade> unidades = new ArrayList<>();
        NodeList estabelecimentos = documento.getElementsByTagNameNS("*", "DadosGeraisEstabelecimentoSaude");
        for (int i = 0; i < estabelecimentos.getLength(); i++) {
            unidades.add(lerUnidade((Element) estabelecimentos.item(i)));
        }
        return unidades;
    }

    private static Unidade lerUnidade(Element est) {
        Element endereco = filho(est, "Endereco");

        String logradouro = texto(endereco, "nomeLogradouro");
        String tipoLogradouro = texto(endereco, "descricaoTipoLogradouro");
        if (logradouro != null && tipoLogradouro != null && !logradouro.startsWith(tipoLogradouro)) {
            logradouro = tipoLogradouro + " " + logradouro;
        }

        List<String> telefones = new ArrayList<>();
        NodeList lista = est.getElementsByTagNameNS("*", "Telefone");
        for (int i = 0; i < lista.getLength(); i++) {
            String telefone = formatarTelefone(texto((Element) lista.item(i), "DDD"),
                    texto((Element) lista.item(i), "numeroTelefone"));
            if (telefone != null) {
                telefones.add(telefone);
            }
        }

        return new Unidade(texto(filho(est, "nomeFantasia"), "Nome"), texto(filho(est, "tipoUnidade"), "descricao"),
                logradouro, texto(endereco, "numero"), texto(endereco, "descricaoBairro"), texto(endereco, "numeroCEP"),
                texto(endereco, "nomeMunicipio"), texto(endereco, "siglaUF"), telefones,
                texto(filho(est, "Email"), "descricaoEmail"), "true".equalsIgnoreCase(texto(est, "perteceSistemaSUS")),
                texto(est, "dataAtualizacao"));
    }

    // Primeiro elemento descendente com esse nome (ignorando o prefixo de namespace), ou null.
    private static Element filho(Element pai, String nome) {
        if (pai == null) {
            return null;
        }
        NodeList achados = pai.getElementsByTagNameNS("*", nome);
        return achados.getLength() == 0 ? null : (Element) achados.item(0);
    }

    // Texto do primeiro descendente com esse nome, sem espaços nas pontas; null se ausente ou vazio.
    private static String texto(Element pai, String nome) {
        Element elemento = filho(pai, nome);
        if (elemento == null) {
            return null;
        }
        String valor = elemento.getTextContent().trim();
        return valor.isEmpty() ? null : valor;
    }

    private static String formatarTelefone(String ddd, String numero) {
        if (numero == null) {
            return null;
        }
        String digitos = numero.replaceAll("\\D", "");
        String formatado = digitos.length() > 4
                ? digitos.substring(0, digitos.length() - 4) + "-" + digitos.substring(digitos.length() - 4)
                : digitos;
        return ddd == null ? formatado : "(" + ddd + ") " + formatado;
    }

    private static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static ApiException fonteIndisponivel() {
        return new ApiException(502, "FONTE_INDISPONIVEL",
                "Não foi possível consultar o CNES agora. Tente novamente em instantes.");
    }
}
