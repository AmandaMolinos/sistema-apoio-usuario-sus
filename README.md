# Apoio SUS

Aplicativo para centralizar dados de saúde informados pelo próprio usuário e facilitar o acesso a informações públicas do SUS, como as UPAs de cada município do Estado de São Paulo. Projeto de estudo (TCC).

- **App:** .NET MAUI (C#, XAML) com MVVM escrito à mão, sem biblioteca. Roda no Windows.
- **API:** Java 21 puro (sem Spring), servidor HTTP do JDK e JDBC, com SQLite (padrão) ou MySQL.
- **Documentação:** a especificação, o contrato detalhado da API, as decisões, as fontes públicas e o andamento ficam em documentos locais, fora deste repositório. O resumo das rotas está na seção [Endpoints](#endpoints).

Os dados pessoais são autodeclarados: o app não acessa prontuários oficiais e não faz diagnóstico.

## Sumário

1. [O que já funciona](#o-que-já-funciona)
2. [Como o sistema se encaixa](#como-o-sistema-se-encaixa)
3. [O que é preciso para rodar](#o-que-é-preciso-para-rodar)
4. [Passo a passo para rodar](#passo-a-passo-para-rodar)
5. [Como testar](#como-testar)
6. [Configuração (variáveis de ambiente)](#configuração-variáveis-de-ambiente)
7. [Banco de dados (SQLite e MySQL)](#banco-de-dados-sqlite-e-mysql)
8. [Problemas comuns](#problemas-comuns)
9. [Estrutura do repositório](#estrutura-do-repositório)
10. [Endpoints](#endpoints)
11. [Segurança e limitações](#segurança-e-limitações)

## O que já funciona

| Módulo | Funcionalidades |
| --- | --- |
| Autenticação | Cadastro (com aceite dos Termos de Uso e da Política de Privacidade), login, logout, recuperar e redefinir senha |
| Conta | Ver, editar (nome e data de nascimento) e excluir a conta |
| Perfil de saúde | Peso (kg), altura (m), tipo sanguíneo, alergias e observações; consultar, atualizar e limpar |
| Vacinas | Cadastrar, listar, consultar, editar e excluir registros pessoais |
| Medicamentos | Cadastrar, listar, consultar, editar e excluir medicamentos (nome, dosagem, frequência e data de início; data final opcional), com abas Em uso e Histórico |
| Unidades de saúde | Escolher um município do Estado de São Paulo e ver as UPAs do SUS, com endereço, telefone e e-mail, em Mapa (desenho de modelo) e Lista, com busca e botão Ver rotas (dados do CNES, do Ministério da Saúde; precisa de internet) |
| Orientações de saúde | Orientações gerais (prevenção, doenças e hábitos) com busca e filtro por categoria; o texto fica em `backend-java/src/main/resources/orientacoes.json` |
| Análise de sintomas | Descrever os sintomas em texto livre e receber orientações informativas (cuidados gerais e quando procurar atendimento), com aviso de urgência para sinais de alerta. Regras fixas lidas de `sintomas.json`, sem IA e sem diagnóstico; o texto não é guardado |

**Ainda não feito:** mapa real com a localização das unidades (hoje o mapa é um desenho de modelo), outros tipos de unidade (UBS e hospitais) e outros estados, campanhas de vacinação, vacinas pendentes, lembretes de medicamentos, troca de e-mail e de senha com o usuário logado.

## Como o sistema se encaixa

```text
 App .NET MAUI (Windows)  ──HTTP/JSON──▶  API Java (porta 8080)  ──JDBC──▶  SQLite (arquivo)  ou  MySQL (porta 3306)
                                                  │
                                                  └──SOAP──▶  CNES, Ministério da Saúde (internet)
```

O app **só funciona com a API ligada**: tudo (login, vacinas, medicamentos etc.) passa por ela. O banco e o CNES são usados só pela API. Sem internet, tudo funciona, exceto a busca de UPAs.

## O que é preciso para rodar

### Máquina e sistema

- **Windows 10 (1809) ou mais novo; testado no Windows 11.** O app MAUI deste projeto roda no Windows.
- Conexão com a internet na primeira execução (baixa dependências do Maven e do NuGet) e para a busca de UPAs.
- Portas livres: **8080** (API) e, se usar MySQL, **3306**.

### Ferramentas

| Ferramenta | Versão testada | Para quê | Instalação |
| --- | --- | --- | --- |
| JDK | Microsoft OpenJDK 21 | Compilar e rodar a API | `winget install Microsoft.OpenJDK.21` |
| .NET SDK | 10.0 | Compilar e rodar o app | `winget install Microsoft.DotNet.SDK.10` |
| Workload MAUI | `maui-windows` 10.0.110 | Telas MAUI no Windows | `dotnet workload install maui-windows` |
| Maven | não precisa instalar | Build da API | vem pelo Maven Wrapper (`mvnw.cmd`) |
| MySQL 8 | 8.4 (opcional) | Só se for usar MySQL em vez de SQLite | `winget install Oracle.MySQL` |

Confira as instalações: `java -version` (deve mostrar 21), `dotnet --version` (10.x) e `dotnet workload list` (deve listar `maui-windows`).

Se o terminal não enxergar o Java, defina `JAVA_HOME` na sessão (ajuste o caminho para a sua versão):

```powershell
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.12.101-hotspot"
```

### Visual Studio (alternativa à linha de comando)

Dá para abrir `mobile/ApoioSus.slnx` no Visual Studio com o suporte a .NET 10 e a carga de trabalho **Desenvolvimento para dispositivos móveis com .NET** (MAUI). Escolha o destino **Windows Machine** e inicie. A API precisa estar rodando antes, em outro terminal. O formato `.slnx` não abre em versões antigas do Visual Studio.

### Android (opcional, não testado em execução)

`dotnet workload install maui-android` e o Android SDK. O alvo Android **compila** (`dotnet build -f net10.0-android`), mas o app só foi executado no **Windows**. No emulador Android a API é `10.0.2.2:8080`.

## Passo a passo para rodar

Abra **dois terminais** PowerShell na raiz do repositório. A API sobe primeiro.

### 1. API (terminal 1)

```powershell
cd backend-java
.\mvnw.cmd exec:java
```

Na primeira vez baixa as dependências e leva alguns minutos. Quando aparecer `Apoio SUS (Java puro) em http://localhost:8080`, a API está pronta. Para conferir, abra `http://localhost:8080/api/ping` no navegador: deve responder `{"status":"ok"}`.

Por padrão a API usa **SQLite**: cria sozinha o arquivo `backend-java/apoio_sus_puro.db` e as tabelas. Não precisa instalar banco. Para usar MySQL, veja [Banco de dados](#banco-de-dados-sqlite-e-mysql).

> Inicie a API **de dentro da pasta `backend-java`**, para o arquivo do banco ficar no lugar certo.

### 2. App (terminal 2, com a API rodando)

```powershell
cd mobile\ApoioSus
dotnet build -f net10.0-windows10.0.19041.0
.\bin\Debug\net10.0-windows10.0.19041.0\win-x64\ApoioSus.exe
```

A primeira compilação baixa pacotes e demora. Com o app aberto, o `.exe` fica bloqueado: **feche o app antes de compilar de novo**. A URL da API fica em `mobile/ApoioSus/Services/Api/ApiService.cs` (`localhost:8080` no Windows e `10.0.2.2:8080` no emulador Android).

### 3. Parar tudo

Feche a janela do app e use `Ctrl+C` no terminal da API.

## Como testar

### Testes automatizados da API (100 testes)

```powershell
cd backend-java
.\mvnw.cmd test
```

- Cada classe de teste sobe a API real numa porta livre, com banco temporário, e faz chamadas HTTP de verdade (cadastro, login, vacinas, medicamentos, perfil, conta, recuperação de senha, UPAs, orientações, sintomas etc.).
- Não usam a internet: o CNES é simulado por um servidor local. Orientações e análise de sintomas não dependem de nada externo.
- Resultado esperado: `Tests run: 100, Failures: 0, Errors: 0`. Os relatórios ficam em `backend-java/target/surefire-reports/`.
- Para rodar só uma classe: `.\mvnw.cmd test -Dtest=SintomaTests`.
- O app **não tem testes automatizados**; é testado à mão, pelo roteiro abaixo.

### Teste rápido da API sem o app (PowerShell, com a API rodando)

```powershell
$api = "http://localhost:8080/api"
$email = "teste-$(Get-Random)@exemplo.com"

# cadastro e login
Invoke-RestMethod "$api/auth/cadastro" -Method Post -ContentType "application/json" `
  -Body (@{ nome = "Maria Teste"; email = $email; senha = "senha1234" } | ConvertTo-Json)
$token = (Invoke-RestMethod "$api/auth/login" -Method Post -ContentType "application/json" `
  -Body (@{ email = $email; senha = "senha1234" } | ConvertTo-Json)).token
$h = @{ Authorization = "Bearer $token" }

# rotas protegidas
Invoke-RestMethod "$api/usuarios/me" -Headers $h
Invoke-RestMethod "$api/orientacoes" -Headers $h
Invoke-RestMethod "$api/sintomas/analise" -Method Post -Headers $h -ContentType "application/json" `
  -Body (@{ texto = "febre e tosse" } | ConvertTo-Json)
```

### Roteiro de teste do app (manual)

O app abre na tela de **login** (com **Cadastre-se** e **Esqueceu sua senha?**). Depois do login, o Início tem seis cartões (Meu perfil de saúde, Minhas vacinas, Meus medicamentos, Unidades de saúde, Orientações de saúde e Análise de sintomas) e, embaixo, a barra com as abas **Início, Vacinas, Medicamentos, Unidades e Mais**. A aba **Mais** leva a **Minha conta** e **Sair**.

| # | Onde | O que fazer | O que deve acontecer |
| --- | --- | --- | --- |
| 1 | Login > **Cadastre-se** | Preencher nome, e-mail, senha (mínimo 8) e confirmação, marcar o aceite dos termos (os links abrem os textos) e tocar em **Cadastrar** | Aviso de conta criada; volta ao login. Sem marcar os termos, mostra erro |
| 2 | Login | Entrar com a conta criada. O olho mostra e esconde a senha | Abre o Início com "Olá, <nome>!" |
| 3 | **Minhas vacinas** | **+ Adicionar**: nome, data (dd/mm/aaaa), lote e local; salvar. Tocar na vacina para editar ou excluir | A vacina aparece em cartão; edição e exclusão funcionam |
| 4 | **Meus medicamentos** | **+ Adicionar**: nome, dosagem, frequência, início e, opcional, data final. Cadastre um com data final no passado | Os ativos ficam em **Em uso**; o de data final passada vai para **Histórico** |
| 5 | **Meu perfil de saúde** | **Editar** (em Informações de saúde): peso, altura, tipo sanguíneo, alergias, observações; salvar. **Limpar perfil** zera | Os valores aparecem na leitura ("68 kg", "1,65 m") |
| 6 | **Unidades de saúde** (precisa de internet) | Buscar "Santos", tocar no município; alternar **Mapa** e **Lista**; **Ver rotas** | Lista de UPAs do SUS; o botão abre o endereço no navegador |
| 7 | **Orientações de saúde** | Filtrar por **Todas**, **Prevenção**, **Doenças** ou **Hábitos** e usar a busca | A lista muda conforme o filtro e o texto |
| 8 | **Análise de sintomas** | Digitar "febre e dor de cabeça" e **Analisar sintomas**; depois "falta de ar" e "me sinto estranho" | Orientações por sintoma; aviso vermelho de urgência (SAMU 192) para "falta de ar"; mensagem de "não reconhecido" para o último |
| 9 | **Mais > Minha conta** | Editar o nome e salvar; o e-mail aparece só para leitura | "Dados salvos." |
| 10 | Login > **Esqueceu sua senha?** | Informar o e-mail e pedir o código; copiar o código do **terminal da API** (linha `[RECUPERACAO DE SENHA]`, formato `XXXX-XXXX-XXXX`) e usar **Já tenho o código** para definir a nova senha | Não há envio de e-mail; a senha nova funciona e a antiga deixa de valer |
| 11 | **Mais > Sair** | Tocar em **Sair** | Volta ao login |
| 12 | **Mais > Minha conta > Excluir minha conta** | Confirmar a exclusão | A conta e os dados são apagados; volta ao login |

Dica: teste também os erros (e-mail repetido no cadastro, senha curta, data inválida, campos vazios) para ver as mensagens em vermelho.

## Configuração (variáveis de ambiente)

Todas são opcionais; os valores padrão servem para rodar com SQLite. Defina na sessão do PowerShell antes de `.\mvnw.cmd exec:java`.

| Variável | Padrão | Para quê |
| --- | --- | --- |
| `API_PORT` | `8080` | Porta da API (se mudar, ajuste também `ApiService.cs` no app) |
| `DB_URL` | `jdbc:sqlite:apoio_sus_puro.db?foreign_keys=on&busy_timeout=5000` | Banco: SQLite ou `jdbc:mysql:...` |
| `DB_USUARIO`, `DB_SENHA` | vazios | Usuário e senha do MySQL (acrescentados à `DB_URL` pela API) |
| `SESSAO_HORAS_VALIDADE` | `24` | Duração do login, em horas |
| `RECUPERACAO_MINUTOS_VALIDADE` | `30` | Validade do código de recuperação de senha, em minutos |
| `CNES_URL`, `CNES_USUARIO`, `CNES_SENHA` | serviço público do CNES | Fonte das UPAs; já têm valor padrão (a credencial é a pública do serviço) |
| `TESTE_MYSQL_URL` | vazia | Só para os testes: roda a suíte no MySQL (veja abaixo) |

## Banco de dados (SQLite e MySQL)

A API escolhe o banco pelo início da `DB_URL`: `jdbc:sqlite:...` (padrão) ou `jdbc:mysql:...`. As tabelas são criadas e atualizadas sozinhas na primeira execução, a partir de `backend-java/src/main/resources/db/migration/<sqlite|mysql>/` (V1 a V3, com o mesmo esquema nos dois). Orientações e sintomas não usam tabela.

- **Zerar os dados:** no SQLite, feche a API e apague `backend-java/apoio_sus_puro.db`; no MySQL, apague e recrie o banco.
- Os dados de um banco **não** são copiados automaticamente para o outro.

### Usando MySQL

1. Tenha um MySQL 8 rodando e crie o banco e um usuário (como administrador do MySQL):

```sql
CREATE DATABASE apoio_sus CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER 'apoio_sus'@'localhost' IDENTIFIED BY 'troque-esta-senha';
GRANT ALL PRIVILEGES ON apoio_sus.* TO 'apoio_sus'@'localhost';
```

2. Suba a API apontando para ele (a senha vai por variável de ambiente, nunca no código):

```powershell
cd backend-java
$env:DB_URL = "jdbc:mysql://localhost:3306/apoio_sus?useUnicode=true&characterEncoding=utf8&connectionTimeZone=LOCAL"
$env:DB_USUARIO = "apoio_sus"
$env:DB_SENHA = "troque-esta-senha"
.\mvnw.cmd exec:java
```

O app não muda: continua falando com a API em `localhost:8080`. Para conferir que está no MySQL, cadastre um usuário no app e consulte a tabela `usuario` do banco `apoio_sus`.

3. **Testes no MySQL (opcional):** crie um segundo banco vazio, por exemplo `apoio_sus_teste`, com permissão para o mesmo usuário, e rode:

```powershell
$env:TESTE_MYSQL_URL = "jdbc:mysql://localhost:3306/apoio_sus_teste?user=apoio_sus&password=troque-esta-senha&useUnicode=true&characterEncoding=utf8&connectionTimeZone=LOCAL"
.\mvnw.cmd test
```

**Os testes apagam todas as tabelas desse banco**, então nunca aponte `TESTE_MYSQL_URL` para o banco de uso. Sem essa variável, os testes usam SQLite temporário.

## Problemas comuns

| Sintoma | Causa provável | O que fazer |
| --- | --- | --- |
| App abre, mas login e listas dão erro de conexão | API desligada ou em outra porta | Suba a API e confira `http://localhost:8080/api/ping` |
| "Rota não encontrada" em uma tela nova | Há uma API **antiga** ainda rodando na porta 8080 | Feche a API antiga (`Ctrl+C` ou encerre o processo Java) e inicie de novo |
| `Address already in use` ao subir a API | Porta 8080 ocupada | Feche o outro processo ou use `API_PORT` |
| `java` ou `mvnw` não reconhecido / versão errada | JDK 21 ausente ou fora do PATH | Instale o JDK 21 e defina `JAVA_HOME` (veja acima) |
| Erro ao compilar o app: arquivo `ApoioSus.exe` em uso | O app ainda está aberto | Feche o app e compile de novo |
| Erro de workload ao compilar o app | MAUI não instalado | `dotnet workload install maui-windows` |
| Ícones ou imagens não aparecem | Build antigo | Recompile o app (`dotnet build`) |
| Unidades de saúde: erro ao buscar | Sem internet ou CNES fora do ar | Confira a conexão e use **Tentar novamente** |
| API não sobe com MySQL: `Communications link failure` / acesso negado | MySQL desligado, URL, usuário ou senha errados | Confira se o MySQL está ligado, a `DB_URL`, `DB_USUARIO` e `DB_SENHA` |
| Usuários "sumiram" ao trocar de banco | Cada banco tem seus próprios dados | Volte à `DB_URL` anterior ou cadastre de novo |

## Estrutura do repositório

```text
backend-java/   API em Java puro (backend principal)
mobile/         App .NET MAUI (mobile/ApoioSus)
backend/        API em Spring Boot, apenas referência (cadastro, login e logout); não é usada
```

Dentro de `backend-java/src/main/java/br/com/apoiosus/`: `controller/` (recebe a requisição), `service/` (regras), `repository/` (SQL), `integration/` (cliente do CNES), `dto/` (entrada e saída), `model/` (dados), mais `Http.java` (rotas, JSON, token) e `Banco.java` (conexão e migrations). O conteúdo fixo fica em `backend-java/src/main/resources/`: `municipios-sp.csv` (municípios de São Paulo), `orientacoes.json` (orientações de saúde) e `sintomas.json` (catálogo da análise de sintomas). Os testes ficam em `backend-java/src/test/`.

Dentro de `mobile/ApoioSus/`: `Views/` (telas XAML e a barra inferior), `ViewModels/` (estado e comandos), `Models/` (dados da API), `Services/` (acesso à API, sessão, datas e números) e `Resources/Images/` (ícones e ilustrações SVG).

## Endpoints

Prefixo `/api`. Rotas protegidas exigem `Authorization: Bearer <token>`. Erros seguem o formato `{type, title, status, detail, codigo, mensagem, erros}`, em que `erros` traz a mensagem de cada campo inválido.

| Rota | Métodos |
| --- | --- |
| `/ping` | GET |
| `/auth/cadastro`, `/auth/login` | POST |
| `/auth/logout`, `/auth/recuperar-senha`, `/auth/redefinir-senha` | POST |
| `/usuarios/me` | GET, PUT, DELETE |
| `/perfil` | GET, PUT, DELETE |
| `/vacinas` | GET, POST |
| `/vacinas/{id}` | GET, PUT, DELETE |
| `/medicamentos` | GET, POST |
| `/medicamentos/{id}` | GET, PUT, DELETE |
| `/unidades-saude/municipios` | GET |
| `/unidades-saude/upas?municipio=&inicio=` | GET |
| `/orientacoes` | GET |
| `/sintomas/analise` | POST |

## Segurança e limitações

- Senha guardada só como hash (PBKDF2-HMAC-SHA256, com salt por senha).
- Sessão por token aleatório; o banco guarda apenas o SHA-256 do token. O logout revoga a sessão e a redefinição de senha encerra todas.
- Login com e-mail inexistente e com senha errada dão a mesma resposta.
- Cada usuário só acessa os próprios registros; o de outro usuário responde 404.
- As unidades de saúde vêm de uma fonte pública (CNES). A API devolve apenas dados de contato e localização da unidade e não repassa dados pessoais que a fonte também envia, como o CPF do diretor. A credencial usada é a pública do serviço e pode ser trocada por variável de ambiente.
- O texto da análise de sintomas não é guardado nem registrado em log.
- Senhas do banco ficam em variáveis de ambiente, nunca no código.
- **Limitações atuais:** HTTP sem TLS (apenas desenvolvimento), sem limite de tentativas no login e na recuperação de senha, e-mail de recuperação não enviado (o código aparece no terminal da API), conteúdo de orientações e sintomas ainda sem revisão formal por profissional de saúde, app executado e testado só no Windows.
