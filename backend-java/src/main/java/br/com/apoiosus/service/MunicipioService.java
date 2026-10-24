package br.com.apoiosus.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import br.com.apoiosus.model.Municipio;

// Municípios do Estado de São Paulo, lidos uma vez de municipios-sp.csv (linhas "codigo;nome").
// Fonte do arquivo: API de localidades do IBGE.
public class MunicipioService {

    private final List<Municipio> municipios;

    public MunicipioService() {
        this.municipios = carregar();
    }

    public List<Municipio> listar() {
        return municipios;
    }

    // Retorna null se o código não for de um município de São Paulo.
    public Municipio buscar(String codigo) {
        for (Municipio municipio : municipios) {
            if (municipio.codigo().equals(codigo)) {
                return municipio;
            }
        }
        return null;
    }

    private static List<Municipio> carregar() {
        try (InputStream arquivo = MunicipioService.class.getResourceAsStream("/municipios-sp.csv")) {
            if (arquivo == null) {
                throw new IllegalStateException("Arquivo municipios-sp.csv não encontrado.");
            }
            BufferedReader leitor = new BufferedReader(new InputStreamReader(arquivo, StandardCharsets.UTF_8));
            List<Municipio> lista = new ArrayList<>();
            String linha;
            while ((linha = leitor.readLine()) != null) {
                if (!linha.isBlank()) {
                    String[] partes = linha.split(";", 2);
                    lista.add(new Municipio(partes[0].trim(), partes[1].trim()));
                }
            }
            return List.copyOf(lista);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
