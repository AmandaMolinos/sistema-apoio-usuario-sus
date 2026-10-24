namespace ApoioSus.ViewModels;

// Mostra um texto fixo: os Termos de Uso ou a Política de Privacidade (rota "documento?tipo=termos|privacidade").
[QueryProperty(nameof(Tipo), "tipo")]
public class DocumentoViewModel : ViewModelBase
{
	public DocumentoViewModel()
	{
		VoltarCommand = new Comando(() => Shell.Current.GoToAsync(".."));
	}

	public Comando VoltarCommand { get; }

	private string _titulo = "";
	public string Titulo { get => _titulo; private set => SetProperty(ref _titulo, value); }

	private string _texto = "";
	public string Texto { get => _texto; private set => SetProperty(ref _texto, value); }

	public string Tipo
	{
		set
		{
			Titulo = value == "privacidade" ? "Política de Privacidade" : "Termos de Uso";
			Texto = value == "privacidade" ? Privacidade : Termos;
		}
	}

	private const string Termos = """
		1. Sobre o aplicativo
		O Apoio SUS é um projeto acadêmico (TCC) que ajuda você a organizar informações de saúde e facilita o acesso a informações públicas do SUS. Não é um serviço oficial do Ministério da Saúde.

		2. Dados informados por você
		Os dados de perfil, vacinas e medicamentos são informados por você. O aplicativo não acessa prontuários nem sistemas oficiais. Você é responsável por manter essas informações corretas.

		3. Orientações e análise de sintomas
		O conteúdo de orientações e a análise de sintomas são apenas informativos. Não fazem diagnóstico, não indicam tratamento e não substituem a avaliação de um profissional de saúde. Em emergência, ligue 192 (SAMU).

		4. Sua conta
		Guarde sua senha em sigilo. Você pode editar seus dados ou excluir a conta a qualquer momento, em Mais > Minha conta.

		5. Disponibilidade
		Por ser um projeto de estudo, o aplicativo pode mudar ou ficar indisponível sem aviso.
		""";

	private const string Privacidade = """
		1. Quais dados são guardados
		Nome, e-mail, senha e, se você informar, a data de nascimento, além do que você cadastrar no perfil de saúde, nas vacinas e nos medicamentos. A senha é guardada apenas como um código irreversível (hash), nunca como texto.

		2. Para que servem
		Para mostrar os seus dados a você. Cada usuário só acessa os próprios registros, e os dados não são vendidos nem compartilhados com terceiros.

		3. Consulta de unidades de saúde
		Ao buscar unidades, o aplicativo consulta um serviço público do Ministério da Saúde (CNES) e envia apenas o município escolhido. Nenhum dado seu é enviado.

		4. Análise de sintomas
		O texto que você digita na análise de sintomas é usado só para gerar a resposta e não é guardado.

		5. Seus direitos
		Você pode ver e corrigir seus dados no aplicativo e excluir a conta. Ao excluir, os seus dados cadastrados são apagados.

		6. Limitações desta versão
		Esta é uma versão de estudo: a comunicação com o servidor ainda não usa conexão criptografada (HTTPS). Não informe dados que você não queira registrar em um ambiente de testes.
		""";
}
