package br.com.apoiosus.model;

import java.util.List;

// Estabelecimento de saúde como o app precisa dele. Só campos públicos de contato e localização:
// o CNES também devolve CPF e nome do diretor, que nunca são lidos.
public record Unidade(String nome, String tipo, String logradouro, String numero, String bairro, String cep,
        String municipio, String uf, List<String> telefones, String email, boolean atendeSus, String atualizadoEm) {
}
