# Sistema de Apoio ao Usuário do SUS — Backend

Backend do Sistema de Apoio ao Usuário do SUS, desenvolvido para disponibilizar funcionalidades de autenticação, gerenciamento de usuários, perfil de saúde, vacinas e medicamentos.

**Tecnologias:** Java 25, Spring Boot, Spring Data JPA, MySQL, Spring Security, JWT, BCrypt, JUnit 5 e Mockito.

## Funcionalidades

### Autenticação e gerenciamento de usuários
- Cadastro de usuários com nome, e-mail, senha e data de nascimento.
- Validação dos dados informados no cadastro.
- Criptografia de senhas utilizando BCrypt.
- Autenticação de usuários por e-mail e senha.
- Geração de token JWT para autenticação nas rotas protegidas.
- Consulta dos dados básicos do usuário autenticado.
- Criação automática do perfil de saúde durante o cadastro.

### Perfil de saúde
- Consulta do perfil de saúde do usuário autenticado.
- Atualização das informações de saúde.
- Armazenamento de peso, altura, tipo sanguíneo, alergias e observações médicas.

### Gerenciamento de vacinas
- Cadastro de vacinas.
- Listagem das vacinas cadastradas pelo usuário autenticado.
- Consulta individual de uma vacina.
- Atualização dos dados de uma vacina.
- Exclusão de vacinas.
- Validação dos dados informados nas requisições.

As informações de uma vacina incluem nome, data de aplicação, lote e local de aplicação.

### Gerenciamento de medicamentos
- Cadastro de medicamentos.
- Listagem dos medicamentos cadastrados pelo usuário autenticado.
- Consulta individual de um medicamento.
- Atualização dos dados de um medicamento.
- Exclusão de medicamentos.
- Validação dos dados informados nas requisições, incluindo a consistência entre as datas de início e término.

As informações de um medicamento incluem nome, dosagem, frequência, data de início e data de término, que pode ser nula quando não houver uma data definida.

### Persistência e testes
- Persistência de dados em banco de dados MySQL por meio do Spring Data JPA.
- Organização do código seguindo os princípios de Clean Architecture e SOLID.
- Tratamento centralizado de exceções da API.
- Testes unitários de casos de uso utilizando JUnit 5 e Mockito.

## Tecnologias e ferramentas

- **Java 25:** linguagem de programação do backend.
- **Spring Boot:** estrutura principal da aplicação.
- **Spring Security:** configuração de segurança e proteção dos endpoints.
- **JWT:** autenticação por token.
- **BCrypt:** armazenamento seguro de senhas por hash.
- **Spring Data JPA e Hibernate:** mapeamento objeto-relacional e persistência.
- **MySQL 8+:** banco de dados relacional.
- **JUnit 5 e Mockito:** testes unitários.

## Arquitetura do projeto

O backend utiliza Clean Architecture para separar as responsabilidades e facilitar a manutenção e os testes.

- **domain:** entidades e regras de negócio.
- **application:** casos de uso e portas que definem os contratos necessários à aplicação.
- **infrastructure:** implementação da persistência com JPA/MySQL, segurança, JWT, criptografia e configurações.
- **interfaces:** controllers REST, DTOs de requisição e resposta e tratamento de erros.

Essa organização busca manter as regras de negócio independentes dos detalhes de infraestrutura e da comunicação HTTP.

## Pré-requisitos

Antes de executar o projeto, instale:

- JDK 25.
- Maven 3.9 ou superior.
- MySQL 8 ou superior.
- MySQL Workbench ou outra ferramenta para executar scripts SQL.

## Configuração do ambiente

### 1. Preparar o banco de dados

Execute o script `database/schema.sql` no MySQL Workbench para criar a estrutura inicial do banco de dados.

Se o banco já existia e o cadastro de vacina falha com `Field 'nome' doesn't have a default value`, pare a aplicação e execute `database/migrations/001_corrigir_nome_vacina.sql` no MySQL Workbench, usando o mesmo banco configurado em `DB_URL`. Se o nome do banco for diferente, ajuste o `USE` no script. Depois reinicie a aplicação.

A migração renomeia `nome` para `nome_vacina` quando existe apenas a coluna antiga. Quando ambas existem, preenche os valores nulos de `nome_vacina` com o nome antigo e torna `nome` opcional, preservando seus dados. Pode ser executada novamente após a conclusão. O script `schema.sql` cria tabelas novas; `CREATE TABLE IF NOT EXISTS` não corrige tabelas existentes.

### 2. Configurar as variáveis de ambiente

No PowerShell, configure as variáveis utilizadas pela aplicação:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/apoio_usuario_sus?useSSL=false&serverTimezone=UTC"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="sua_senha_mysql"
$env:JWT_SECRET="SUA_CHAVE_BASE64_FORTE"
```

Substitua os valores de conexão pelos dados do seu ambiente. Não compartilhe senhas ou chaves secretas.

Para gerar uma chave aleatória de 32 bytes codificada em Base64, execute no JShell ou em uma classe Java:

```java
System.out.println(
    java.util.Base64.getEncoder().encodeToString(
        new java.security.SecureRandom().generateSeed(32)
    )
);
```

Configure o valor gerado na variável `JWT_SECRET`, de acordo com o mecanismo de validação de chave implementado no projeto.

### 3. Executar a aplicação

Na raiz do backend, execute:

```bash
mvn spring-boot:run
```

Para executar os testes automatizados:

```bash
mvn test
```

## Endpoints da API

As rotas de cadastro e login são públicas. As demais rotas exigem autenticação por JWT.

### Autenticação e usuários

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/api/auth/cadastro` | Público | Cadastra um usuário e cria seu perfil de saúde |
| POST | `/api/auth/login` | Público | Autentica o usuário e retorna um JWT |
| GET | `/api/usuarios/me` | JWT | Consulta os dados básicos do usuário autenticado |

### Perfil de saúde

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| GET | `/api/perfil` | JWT | Consulta o perfil de saúde |
| PUT | `/api/perfil` | JWT | Atualiza o perfil de saúde |

### Vacinas

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/api/vacinas` | JWT | Cadastra uma vacina |
| GET | `/api/vacinas` | JWT | Lista as vacinas do usuário |
| GET | `/api/vacinas/{id}` | JWT | Consulta uma vacina pelo ID |
| PUT | `/api/vacinas/{id}` | JWT | Atualiza uma vacina |
| DELETE | `/api/vacinas/{id}` | JWT | Exclui uma vacina |

### Medicamentos

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/api/medicamentos` | JWT | Cadastra um medicamento |
| GET | `/api/medicamentos` | JWT | Lista os medicamentos do usuário |
| GET | `/api/medicamentos/{id}` | JWT | Consulta um medicamento pelo ID |
| PUT | `/api/medicamentos/{id}` | JWT | Atualiza um medicamento |
| DELETE | `/api/medicamentos/{id}` | JWT | Exclui um medicamento |

## Exemplos de requisições

Os exemplos a seguir ilustram os formatos esperados para as operações da API.

### Cadastro de usuário

**Requisição:** `POST /api/auth/cadastro`

```json
{
  "nome": "Maria da Silva",
  "email": "maria@example.com",
  "senha": "UmaSenhaForte123",
  "dataNascimento": "1995-04-23"
}
```

### Login

**Requisição:** `POST /api/auth/login`

```json
{
  "email": "maria@example.com",
  "senha": "UmaSenhaForte123"
}
```

Utilize o token JWT retornado no login no cabeçalho das requisições protegidas:

```http
Authorization: Bearer <token>
```

### Atualização do perfil de saúde

**Requisição:** `PUT /api/perfil`

```json
{
  "peso": 68.5,
  "altura": 1.65,
  "tipoSanguineo": "O+",
  "alergias": "Dipirona",
  "observacoesMedicas": "Informação opcional"
}
```

### Cadastro de vacina

**Requisição:** `POST /api/vacinas`

```json
{
  "nome": "Influenza",
  "dataAplicacao": "2025-04-10",
  "lote": "LOTE123",
  "localAplicacao": "UBS Central"
}
```

### Cadastro de medicamento

**Requisição:** `POST /api/medicamentos`

```json
{
  "nome": "Medicamento A",
  "dosagem": "500 mg",
  "frequencia": "a cada 8 horas",
  "dataInicio": "2025-04-10",
  "dataFim": null
}
```

## Segurança e isolamento dos dados

- As senhas são armazenadas utilizando hash BCrypt, em vez de texto puro.
- As rotas protegidas exigem um token JWT válido.
- O usuário das operações autenticadas é identificado a partir do contexto de autenticação, sem depender de um `usuarioId` enviado pelo cliente.
- As operações de vacinas e medicamentos devem restringir a consulta, atualização e exclusão aos registros pertencentes ao usuário autenticado.
- A chave JWT e as credenciais do banco de dados devem ser mantidas fora do código-fonte.

Como o sistema pode armazenar informações de saúde, é necessário proteger esses dados e implementar controles adequados de acesso e privacidade antes de utilizar informações reais.

## Observações sobre o banco de dados

O projeto utiliza `ddl-auto=update` para facilitar o desenvolvimento acadêmico. Com essa configuração, o Hibernate pode atualizar a estrutura do banco de dados a partir das entidades mapeadas.

Após adicionar novas entidades, confira se as tabelas e colunas foram criadas corretamente no MySQL. Em ambientes de produção, recomenda-se utilizar ferramentas de versionamento de schema, como Flyway ou Liquibase, além de HTTPS, gerenciamento seguro de segredos, limitação de requisições e revisão de segurança.

## Escopo e limitações

Este repositório documenta as funcionalidades do backend. As telas e a integração visual do aplicativo .NET MAUI não fazem parte do escopo deste backend.

A disponibilidade efetiva de cada endpoint, as regras de validação e o comportamento da persistência devem ser confirmados por meio da execução da aplicação e dos testes automatizados no ambiente configurado.
