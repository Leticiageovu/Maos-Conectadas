# Mãos Conectadas

Sistema web para gestão de produtos, clientes, encomendas e vendas de pequenos negócios artesanais, com foco em organização de catálogo, controle de estoque e acompanhamento do fluxo comercial.

## Sobre o projeto

O Mãos Conectadas foi desenvolvido como um projeto acadêmico para apoiar artesãs, artesãos e microempreendedores no controle de produtos, variações de itens, clientes e encomendas. A ideia central é centralizar informações que normalmente ficam dispersas em planilhas, cadernos e mensagens, oferecendo uma aplicação simples, funcional e responsiva.

A plataforma permite criar contas independentes por usuário, manter um catálogo de produtos, registrar clientes, acompanhar encomendas pendentes e registrar vendas com atualização de estoque, tudo em um único ambiente.

## Objetivo

Desenvolver um MVP funcional para apoiar as operações essenciais de:

- cadastro de produtos;
- consulta de catálogo;
- controle de estoque;
- gestão de clientes;
- registro de encomendas.
- registro de vendas e consulta do histórico comercial.

## Funcionalidades

- Autenticação com cadastro e login de usuário.
- Cadastro e consulta de produtos com nome, descrição, categoria, preço, imagem opcional e quantidade.
- Cadastro de variações de produtos (como tamanho e cor) com controle individual de quantidade.
- Pesquisa de produtos por nome.
- Atualização de estoque de uma variação específica.
- Gestão de clientes com nome, telefone, e-mail e observações.
- Edição e exclusão de clientes.
- Registro de encomendas vinculadas a um cliente e a um produto.
- Controle de status pendente das encomendas.
- Registro de vendas com baixa automática do estoque e histórico por conta.
- Consulta do histórico de vendas com data, produto e variação, quantidade, preço unitário e total.
- Dashboard inicial com totais de clientes, produtos, unidades em estoque, encomendas e vendas, além do total vendido.
- Atalho com ícone de caderno no dashboard para consultar, editar e excluir lembretes persistidos por conta.
- Acesso isolado por conta de usuário, sem compartilhamento de dados entre contas.

## Stack tecnológica

### Frontend
- React 19
- TypeScript
- Vite
- Axios
- React Router DOM

### Backend
- Java 21
- Spring Boot 3.5.x
- Spring Web
- Spring Data JPA
- Spring Security
- Spring Validation
- Spring Actuator

### Banco de dados
- PostgreSQL (desenvolvimento local e produção)
- H2 (somente testes automatizados)

### Testes
- JUnit 5
- Mockito
- Spring Security Test
- JaCoCo

## Arquitetura

O projeto segue uma arquitetura de monólito em camadas:

- Frontend: interface web para navegação, formulários e consumo da API.
- Controller: expõe endpoints REST e valida entrada HTTP.
- Service: centraliza regras de negócio e coordena as operações.
- Repository: acesso e persistência dos dados via Spring Data JPA.
- Banco de dados: armazenamento principal dos dados da aplicação.

## Estrutura do repositório

- `backend/`: API REST em Spring Boot.
- `frontend/`: aplicação web em React + TypeScript.
- `docs/`: documentação de requisitos, arquitetura e validação.
- `docker-compose.yml`: configuração local do PostgreSQL.
- `.github/workflows/ci.yml`: pipeline de integração contínua.

## Requisitos

### Pré-requisitos

- Java 21+
- Maven (ou uso do wrapper do projeto)
- Node.js 20+
- npm
- PostgreSQL 17 ou superior (instalado localmente ou via Docker)
- Docker (opcional)

### Acesso e regras de negócio

Ao iniciar a aplicação, o usuário precisa criar uma conta com nome, celular, e-mail e senha. As senhas são armazenadas com hash e cada conta acessa apenas seus próprios produtos, clientes, encomendas e vendas.

Produtos que não possuem conta associada não são exibidos para evitar compartilhamento entre usuários.

Encomendas são registradas como pendentes e não alteram o estoque automaticamente. Vendas são registradas separadamente: ao confirmar uma venda, a quantidade vendida é descontada da variação selecionada (ou do estoque do produto sem variações). Não é permitido vender mais unidades do que as disponíveis. O sistema salva no histórico os dados do produto e da variação, a quantidade, o preço unitário vigente no momento da venda, o valor total e a data e hora. O registro fica associado à conta que fez a venda; não é necessário associar um cliente.

## Executando o projeto

### 1) Clonando o repositório

```powershell
git clone <url-do-repositorio>
cd Maos-Conectadas
```

### 2) Backend e PostgreSQL

O PostgreSQL é o banco padrão da aplicação. O H2 é usado somente pelos testes automatizados.

#### Usando um PostgreSQL já instalado no Windows

No pgAdmin, conecte ao servidor como administrador e, no Query Tool conectado ao banco `postgres`, verifique se o usuário e o banco da aplicação já existem:

```sql
SELECT rolname FROM pg_roles WHERE rolname = 'maos';
SELECT datname FROM pg_database WHERE datname = 'maos_conectadas';
```

Se o usuário não existir, crie-o sem definir uma senha no script:

```sql
CREATE ROLE maos WITH LOGIN;
```

Defina uma senha forte e exclusiva nas propriedades do usuário no pgAdmin. Não salve senhas em scripts SQL, arquivos versionados ou mensagens.

Se o banco ainda não existir, crie-o. Se já existir, não o apague nem recrie; confirme que o usuário `maos` é seu proprietário:

```sql
CREATE DATABASE maos_conectadas OWNER maos;
-- Se o banco já existir e pertencer a outro usuário:
-- ALTER DATABASE maos_conectadas OWNER TO maos;
```

Use uma senha forte e exclusiva, mantida fora do projeto. A aplicação exige `DATABASE_PASSWORD` no ambiente antes de iniciar.

Com o PostgreSQL ativo, execute:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Para usar o PostgreSQL instalado diretamente no Windows (porta `5432`), configure a URL e a senha reais dessa instalação no mesmo terminal antes de iniciar o backend:

```powershell
$env:DATABASE_URL="jdbc:postgresql://localhost:5432/maos_conectadas"
$env:DATABASE_USERNAME="maos"
$env:DATABASE_PASSWORD="sua-senha-local"
.\mvnw.cmd spring-boot:run
```

Para validar a conexão, confirme no log do backend que a URL JDBC começa com `jdbc:postgresql:` e consulte `http://localhost:8080/actuator/health`. O status deve ser `UP`. Também é possível testar o acesso diretamente no pgAdmin ou com `psql`.

#### Alternativa: PostgreSQL via Docker

O serviço Docker usa a porta `5433` somente no próprio computador e pode coexistir com a instalação local do PostgreSQL na porta `5432`. Na raiz do projeto, crie o arquivo local de configuração:

```powershell
Copy-Item .env.example .env
# Edite .env e substitua o valor de exemplo por uma senha forte e exclusiva.
docker compose up -d postgres
cd backend
$env:DATABASE_URL="jdbc:postgresql://localhost:5433/maos_conectadas"
$env:DATABASE_USERNAME="maos"
$env:DATABASE_PASSWORD=(Get-Content ..\.env | Where-Object { $_ -match '^POSTGRES_PASSWORD=' } | ForEach-Object { $_ -replace '^POSTGRES_PASSWORD=', '' })
.\mvnw.cmd spring-boot:run
```

O Compose cria o banco `maos_conectadas` e o usuário `maos` usando `POSTGRES_PASSWORD` do arquivo `.env`. O arquivo `.env` é ignorado pelo Git; não compartilhe nem versione a senha. Se uma senha real já tiver sido publicada, troque-a também no PostgreSQL.

A aplicação aceita `CORS_ALLOWED_ORIGIN` para configurar as origens permitidas pelo backend. Por padrão, as URLs locais `http://localhost:5173` e `http://127.0.0.1:5173` são aceitas. Para configurar mais de uma origem, separe-as por vírgula.

### 3) Frontend

No diretório do frontend:

```powershell
cd frontend
npm install
npm run dev -- --host 0.0.0.0
```

A interface estará disponível em:

- `http://localhost:5173`

O Vite configura um proxy para `/api`, encaminhando as requisições para o backend em `http://localhost:8080` por padrão.

Se desejar apontar para outro backend em desenvolvimento, defina a variável de ambiente antes de iniciar o frontend:

```powershell
$env:VITE_BACKEND_URL="http://localhost:8080"
```

Também é possível definir a URL do backend diretamente para o cliente:

```powershell
$env:VITE_API_URL="http://localhost:8080"
```

## Configuração do PostgreSQL via Docker

A configuração do PostgreSQL está em `docker-compose.yml`:

```yaml
services:
  postgres:
    image: postgres:17
    environment:
      POSTGRES_DB: maos_conectadas
      POSTGRES_USER:
      POSTGRES_PASSWORD:
    ports:
```

## Fluxo principal da aplicação

O sistema oferece os seguintes módulos principais:

- Cadastro e autenticação de usuários
- Catálogo de produtos
- Cadastro de novos itens e variações
- Atualização de estoque
- Cadastro de clientes
- Registro de encomendas
- Registro e histórico de vendas
- Consulta do histórico comercial do usuário

### Registrar uma venda

1. Abra **Consultar catálogo** e selecione um produto.
2. Clique em **Vender**.
3. Escolha a variação, quando houver mais de uma, e informe a quantidade.
4. Clique em **Vendido** para salvar a venda e atualizar o estoque.
5. Consulte os registros na tela **Vendas**, acessível pelo menu, e veja os totais atualizados no dashboard.

O histórico de vendas é persistido no banco de dados e separado por conta. O preço unitário é copiado do preço do produto no momento em que a venda é registrada; alterações futuras no cadastro do produto não mudam as vendas já realizadas.

Após entrar, o usuário é direcionado ao dashboard. Os cartões mostram os dados da conta e permitem abrir as telas correspondentes. A quantidade em estoque soma as quantidades das variações quando existirem e usa a quantidade do produto sem variações nos demais casos. O total vendido soma os valores totais das vendas registradas. O atalho **Bloco de notas** abre a tela de lembretes salvos, onde é possível consultar, editar, adicionar e excluir o conteúdo. Cada conta mantém seus próprios lembretes, limitados a 5.000 caracteres.

## Endpoints da API

Cadastro, login e obtenção do token CSRF são públicos. Os demais endpoints exigem uma sessão autenticada.

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/auth/csrf` | Obtém o token CSRF |
| POST | `/api/auth/register` | Cria uma conta |
| POST | `/api/auth/login` | Inicia uma sessão |
| POST | `/api/auth/logout` | Encerra a sessão |
| GET | `/api/auth/me` | Consulta a conta autenticada |
| GET | `/api/dashboard/notes` | Consulta os lembretes do dashboard |
| PUT | `/api/dashboard/notes` | Salva os lembretes do dashboard (até 5.000 caracteres) |
| DELETE | `/api/dashboard/notes` | Exclui o bloco de lembretes da conta autenticada |
| GET | `/api/products` | Lista produtos; aceita `?search=` |
| GET | `/api/products/{id}` | Consulta um produto |
| POST | `/api/products` | Cadastra um produto |
| PUT | `/api/products/{id}` | Atualiza um produto |
| DELETE | `/api/products/{id}` | Remove um produto |
| GET | `/api/clients` | Lista clientes da conta |
| POST | `/api/clients` | Cadastra um cliente |
| PUT | `/api/clients/{id}` | Atualiza um cliente |
| DELETE | `/api/clients/{id}` | Remove um cliente |
| GET | `/api/orders` | Lista encomendas da conta |
| POST | `/api/orders` | Registra uma encomenda pendente |
| GET | `/api/sales` | Lista o histórico de vendas da conta |
| POST | `/api/sales` | Registra uma venda e baixa o estoque |

## Validação e qualidade

O projeto já conta com estrutura para testes automatizados e cobertura com JaCoCo. A integração contínua está configurada em:

- `.github/workflows/ci.yml`

A verificação responsiva registrada cobre as telas de login e cadastro em larguras de 320, 390, 768 e 1440 px, sem rolagem horizontal. As telas internas autenticadas ainda não foram verificadas; consulte `docs/validacao/responsividade.md` para o escopo.

## Documentação adicional

- `docs/requisitos.md`: requisitos funcionais e não funcionais.
- `docs/arquitetura.md`: visão da arquitetura e endpoints principais.
- `docs/validacao/responsividade.md`: escopo e resultado da verificação responsiva.
- `frontend/README.md`: documentação específica do front-end.

## Status do projeto

*Concluído*  — o MVP está funcional, com as funcionalidades descritas neste README implementadas e validado com build do front-end, lint e testes automatizados do back-end.

Projeto acadêmico de Análise e Desenvolvimento de Sistemas.

### Desenvolvido por Letícia Geovú.
