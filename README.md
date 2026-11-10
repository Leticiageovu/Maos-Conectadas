# Mãos Conectadas

Sistema web para gestão de produtos, clientes e encomendas de pequenos negócios artesanais, com foco em organização de catálogo, controle de estoque e acompanhamento do fluxo comercial.

## Sobre o projeto

O Mãos Conectadas foi desenvolvido como um projeto acadêmico para apoiar artesãs, artesãos e microempreendedores no controle de produtos, variações de itens, clientes e encomendas. A ideia central é centralizar informações que normalmente ficam dispersas em planilhas, cadernos e mensagens, oferecendo uma aplicação simples, funcional e responsiva.

A plataforma permite criar contas independentes por usuário, manter um catálogo de produtos, registrar clientes e acompanhar pedidos pendentes, tudo em um único ambiente.

## Objetivo

Desenvolver um MVP funcional para apoiar as operações essenciais de:

- cadastro de produtos;
- consulta de catálogo;
- controle de estoque;
- gestão de clientes;
- registro de encomendas.

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
- PostgreSQL (produção/recomendado)
- H2 (desenvolvimento local e testes)

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
- Docker (opcional, mas recomendado para PostgreSQL)

### Acesso e regras de negócio

Ao iniciar a aplicação, o usuário precisa criar uma conta com nome, celular, e-mail e senha. As senhas são armazenadas com hash e cada conta acessa apenas seus próprios produtos, clientes e encomendas.

Produtos que não possuem conta associada não são exibidos para evitar compartilhamento entre usuários.

Encomendas são registradas como pendentes e não alteram o estoque automaticamente.

## Executando o projeto

### 1) Clonando o repositório

```powershell
git clone <url-do-repositorio>
cd Maos-Conectadas
```

### 2) Backend

#### Opção A: usar H2 (padrão)

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Esse modo usa H2 em arquivo e mantém dados entre reinicializações. O banco será salvo localmente dentro da pasta `backend`.

#### Opção B: usar PostgreSQL via Docker

```powershell
docker compose up -d postgres
```

Em seguida:

```powershell
$env:DATABASE_URL="jdbc:postgresql://localhost:5432/maos_conectadas"
$env:DATABASE_USERNAME="maos"
$env:DATABASE_PASSWORD="maos"
cd backend
.\mvnw.cmd spring-boot:run
```

Observação: a aplicação também aceita as variáveis `CORS_ALLOWED_ORIGIN` e outras configurações definidas em `application.properties`.

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

## Banco de dados local

A configuração do PostgreSQL está em `docker-compose.yml`:

```yaml
services:
  postgres:
    image: postgres:17
    environment:
      POSTGRES_DB: maos_conectadas
      POSTGRES_USER: maos
      POSTGRES_PASSWORD: maos
    ports:
      - "5432:5432"
```

## Fluxo principal da aplicação

O sistema oferece os seguintes módulos principais:

- Cadastro e autenticação de usuários
- Catálogo de produtos
- Cadastro de novos itens e variações
- Atualização de estoque
- Cadastro de clientes
- Registro de encomendas
- Consulta do histórico comercial do usuário

## Validação e qualidade

O projeto já conta com estrutura para testes automatizados e cobertura com JaCoCo. A integração contínua está configurada em:

- `.github/workflows/ci.yml`

## Documentação adicional

- `docs/requisitos.md`: requisitos funcionais e não funcionais.
- `docs/arquitetura.md`: visão da arquitetura e endpoints principais.
- `frontend/README.md`: documentação específica do front-end.

## Status do projeto

Em desenvolvimento, com foco em entrega de um MVP funcional para gestão de estoque e pedidos em negócios artesanais.

Projeto acadêmico de Análise e Desenvolvimento de Sistemas.
