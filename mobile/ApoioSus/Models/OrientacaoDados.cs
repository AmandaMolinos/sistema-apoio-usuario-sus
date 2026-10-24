namespace ApoioSus.Models;

// Orientação de saúde como a API devolve.
public record OrientacaoDados(int Id, string Titulo, string Resumo, string Categoria, string Icone);

// Resposta de GET /api/orientacoes.
public record OrientacoesResposta(List<OrientacaoDados>? Itens);
