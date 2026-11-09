# Mãos Conectadas

Sistema web responsivo para cadastro de produtos, consulta de catálogo e controle de estoque, desenvolvido para apoiar artesãos e microempreendedores.

### Sobre o projeto

O Mãos Conectadas é um projeto desenvolvido no contexto do curso de Análise e Desenvolvimento de Sistemas, com a proposta de facilitar a organização de produtos e o controle de quantidades disponíveis em pequenos negócios artesanais.

A solução busca substituir controles manuais e informações dispersas por um ambiente digital simples, permitindo que o usuário cadastre produtos, organize suas variações e acompanhe as quantidades disponíveis em estoque.

### Objetivo
Desenvolver um MVP (Produto Mínimo Viável) funcional, com foco nas operações essenciais de cadastro, consulta e atualização de estoque.

### Funcionalidades
- Autenticação de usuários.
- Cadastro de produtos com nome, descrição, categoria, preço e foto opcional.
- Cadastro de variações de produtos, como tamanho e cor.
- Registro da quantidade disponível por variação.
- Consulta do catálogo de produtos.
- Atualização da quantidade de uma variação específica.
- Agenda de clientes com celular, e-mail, observações, edição e exclusão.
- Cadastro e consulta de encomendas vinculadas a uma cliente e a um produto, com quantidade, previsão de entrega e status pendente.
- Pesquisa de produtos pelo nome (funcionalidade desejável).

### Tecnologias previstas

#### Front-end
- React 19
- TypeScript 5

#### Back-end
- Java 21
- Spring Boot 3.5.x
- Spring Data JPA
- Spring Security
- Jakarta Bean Validation
- API REST

#### Banco de dados
- PostgreSQL

#### Testes
- JUnit 5
- Mockito

### Arquitetura

O projeto utilizará uma arquitetura de monólito em camadas, com separação de responsabilidades:

- Controller: recebe e encaminha as requisições da API.
- Service: concentra as regras de negócio.
- Repository: realiza o acesso e a persistência dos dados.
- Front-end: apresenta a interface e realiza a comunicação com a API REST.
- Banco de dados: armazena usuários, produtos e suas variações.

### Práticas de engenharia de software

Durante o desenvolvimento, serão aplicadas práticas de:

- Controle de versão com Git e GitHub.
- Organização do código por branches.
- Commits semânticos utilizando Conventional Commits.
- Pull requests e revisão de código.
- Testes automatizados unitários e de integração.
- Integração contínua (CI) para execução automatizada de verificações.
- Registro de melhorias e ajustes identificados durante as validações.

### Etapas de desenvolvimento — Experiência Prática III

1. Configuração do repositório: organização do GitHub, documentação inicial e estrutura do projeto.
2. Implementação: desenvolvimento do front-end, back-end e integração com o banco de dados.
3. Controle de versão: utilização de branches, commits semânticos e pull requests.
4. Qualidade de software: criação de testes automatizados e configuração da pipeline de integração contínua.
5. Validação externa: realização de duas rodadas de avaliação com representantes da comunidade.
6. Melhorias: análise dos feedbacks, registro dos resultados e implementação de ajustes no sistema.

### Público-alvo

Artesãos e microempreendedores que precisam organizar seus produtos e acompanhar as quantidades disponíveis, especialmente aqueles que trabalham com peças artesanais, crochê, costura e produtos personalizados.

### Impacto esperado

O projeto busca contribuir para a organização das atividades produtivas e apoiar o empreendedorismo de pequenos negócios, relacionando-se ao ODS 8 — Trabalho Decente e Crescimento Econômico, especialmente à Meta 8.3.


### Status do projeto
<escape>🚧</escape> Em desenvolvimento — Experiência Prática III.

As funcionalidades, os testes, a integração contínua e as validações serão documentados à medida que forem implementados e realizados.
Projeto acadêmico — Análise e Desenvolvimento de Sistemas.

## Executando o projeto

Ao abrir o sistema, crie uma conta com nome, celular, e-mail e senha. As senhas são armazenadas com hash e cada conta acessa apenas os próprios produtos, clientes e encomendas. Produtos preexistentes sem uma conta associada não são exibidos para evitar compartilhamento entre usuários. Encomendas são registradas como pendentes e não alteram o estoque automaticamente.

### Backend

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Por padrão, o backend usa H2 em arquivo (`backend/maos.mv.db`), preservando contas e produtos entre reinicializações. Para PostgreSQL:

```powershell
docker compose up -d postgres
$env:DATABASE_URL="jdbc:postgresql://localhost:5432/maos_conectadas"
$env:DATABASE_USERNAME="maos"
$env:DATABASE_PASSWORD="maos"
cd backend
.\mvnw.cmd spring-boot:run
```

### Frontend

```powershell
cd frontend
npm install
npm run dev
```

O frontend fica disponível em `http://localhost:5173`. As chamadas `/api` são encaminhadas pelo Vite para `http://localhost:8080`, evitando que o navegador dependa do endereço `localhost` para acessar o backend.

Para usar outro endereço de backend no desenvolvimento, defina `VITE_BACKEND_URL` antes de iniciar o frontend. Para apontar o cliente diretamente a uma API, também é possível definir `VITE_API_URL` com a URL base da API.

## Estrutura

- `backend/`: API REST com Spring Boot.
- `frontend/`: interface web com React e TypeScript.
- `docs/`: arquitetura, requisitos, validação e evidências.
- `.github/workflows/ci.yml`: execução de testes, lint e build.
