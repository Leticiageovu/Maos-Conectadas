# Roadmap do Projeto Mãos Conectadas

## Visão geral

Este roadmap organiza o desenvolvimento do projeto em etapas, com foco em entregar um MVP funcional, melhorar a qualidade do software e preparar a aplicação para uso real em pequenos negócios artesanais.

## Visão das telas e o que acontece em cada uma

O sistema foi pensado para funcionar como uma navegação simples em módulos, com foco em operação rápida e organização do dia a dia do empreendedor artesão.

### 1. Tela de autenticação

**Função:** permitir o acesso do usuário ao sistema.

**O que acontece:**
- o usuário cria uma conta com nome, celular, e-mail e senha;
- o sistema valida os dados e registra a conta;
- o usuário faz login com e-mail e senha;
- a aplicação identifica a conta atual e restringe os dados ao usuário logado;
- cada conta acessa apenas os próprios produtos, clientes e encomendas.

### 2. Tela de catálogo

**Função:** consultar os produtos disponíveis para venda.

**O que acontece:**
- a aplicação lista os produtos cadastrados pela conta atual;
- o usuário pode buscar por nome do produto;
- o catálogo mostra dados como nome, categoria, preço e quantidade;
- a tela serve como consulta rápida do inventário do negócio.

### 3. Tela de cadastro de produto

**Função:** incluir novos itens no sistema.

**O que acontece:**
- o usuário informa nome, descrição, categoria, preço e imagem opcional;
- o sistema permite adicionar variações, como tamanho e cor;
- cada variação pode ter quantidade específica;
- o produto é salvo e passa a aparecer no catálogo e no estoque.

### 4. Tela de estoque

**Função:** controlar a disponibilidade dos produtos.

**O que acontece:**
- o usuário seleciona um produto e uma variação;
- realiza a atualização da quantidade em estoque;
- a aplicação mantém a quantidade correta para o item selecionado;
- a tela ajuda o gestor a saber o que está disponível em cada variação.

### 5. Tela de clientes

**Função:** organizar a agenda de clientes e contatos.

**O que acontece:**
- o usuário cadastra clientes com nome, telefone, e-mail e observações;
- pode editar ou excluir registros;
- a lista centraliza os contatos que serão usados nas encomendas;
- a tela funciona como base comercial do negócio.

### 6. Tela de encomendas

**Função:** registrar pedidos pendentes.

**O que acontece:**
- o usuário associa uma encomenda a um cliente e a um produto;
- define quantidade, data prevista de entrega e observações;
- o pedido é salvo com status pendente;
- a aplicação mostra o histórico de pedidos sem alterar automaticamente o estoque.

### 7. Tela de navegação principal

**Função:** conectar as telas do sistema.

**O que acontece:**
- o usuário alterna entre clientes, catálogo, produtos, encomendas e estoque;
- o menu centraliza a jornada principal da aplicação;
- o fluxo de uso é organizado por módulos para facilitar a operação diária.

## Etapa 1 — Estrutura inicial do projeto

**Objetivo:** preparar a base técnica e organizacional do sistema.

- criação do repositório e organização do código;
- definição da estrutura de pastas do backend e frontend;
- configuração do ambiente local;
- documentação inicial do projeto;
- setup do GitHub e fluxo de versionamento.

**Entregáveis:**
- repositório funcional;
- README inicial com instruções;
- estrutura base para desenvolvimento.

## Etapa 2 — Backend e autenticação

**Objetivo:** implementar a base da aplicação no lado do servidor.

- configuração do Spring Boot;
- criação da autenticação de usuários;
- cadastro e login com senha segura;
- separação de dados por conta de usuário;
- definição de entidades e repositories;
- configuração de segurança e CORS.

**Entregáveis:**
- API REST funcional;
- usuários autenticados;
- proteção das rotas por sessão/autorização.

## Etapa 3 — Gestão de produtos e estoque

**Objetivo:** permitir o cadastro, consulta e controle dos itens do catálogo.

- cadastro de produtos com nome, categoria, preço e descrição;
- suporte a imagem opcional;
- controle de quantidade disponível;
- registro de variações por tamanho e cor;
- atualização de estoque;
- pesquisa por nome do produto.

**Entregáveis:**
- catálogo de produtos funcional;
- controle de variações e disponibilidade;
- busca por produtos.

## Etapa 4 — Gestão de clientes

**Objetivo:** centralizar contatos e informações de clientes.

- cadastro de clientes;
- armazenamento de telefone, e-mail e observações;
- edição e exclusão de registros;
- organização da agenda comercial.

**Entregáveis:**
- lista de clientes funcional;
- manutenção completa dos dados.

## Etapa 5 — Encomendas e fluxo comercial

**Objetivo:** registrar pedidos e acompanhar o processo comercial.

- associação de encomendas a um cliente e a um produto;
- definição de quantidade, data prevista e observações;
- status pendente das encomendas;
- visualização do histórico de pedidos;
- manutenção sem alterar o estoque automaticamente.

**Entregáveis:**
- módulo de encomendas funcional;
- fluxo de pedido simples e claro.

## Etapa 6 — Frontend e experiência do usuário

**Objetivo:** desenvolver uma interface amigável e responsiva para o usuário final.

- criação da navegação principal;
- páginas de produtos, clientes e encomendas;
- formulários de cadastro e edição;
- mensagens de sucesso e erro;
- responsividade para mobile, tablet e desktop.

**Entregáveis:**
- interface funcional;
- fluxo principal da aplicação acessível.

## Etapa 7 — Qualidade e testes

**Objetivo:** garantir confiabilidade e manutenção do sistema.

- testes unitários para serviços principais;
- testes de integração para rotas e regras de negócio;
- cobertura com JaCoCo;
- validação de autenticação e permissões;
- revisão de código e ajustes de qualidade.

**Entregáveis:**
- suíte de testes executável;
- relatório de cobertura;
- menor risco de regressão.

## Etapa 8 — Integração contínua e deploy local

**Objetivo:** automatizar validações e preparar o projeto para execução consistente.

- criação do pipeline de CI;
- execução automática de build e testes;
- validação de lint/build do frontend;
- documentação das etapas de execução local;
- configuração de ambiente para desenvolvimento em equipe.

**Entregáveis:**
- pipeline funcional;
- execução mais confiável em diferentes ambientes.

## Etapa 9 — Validação externa e melhorias

**Objetivo:** avaliar o produto com usuários reais ou representantes da comunidade.

- coleta de feedback sobre usabilidade;
- identificação de pontos de melhoria;
- ajustes de regras de negócio e interface;
- análise de erros e otimizações;
- documentação dos aprendizados.

**Entregáveis:**
- versão melhorada do sistema;
- registro de melhorias com base em avaliação prática.

## Etapa 10 — Evolução do sistema

**Objetivo:** preparar o projeto para novos recursos e maior maturidade.

- melhoria da pesquisa e filtros;
- relatórios de estoque e vendas;
- dashboards operacionais;
- exportação de dados;
- integração com outras ferramentas.

**Entregáveis:**
- base pronta para expansão do produto;
- caminho para evolução de MVP para solução mais robusta.

## Resumo da timeline sugerida

1. Estrutura inicial
2. Autenticação e backend
3. Produtos e estoque
4. Clientes
5. Encomendas
6. Frontend
7. Testes e qualidade
8. CI/CD
9. Validação e ajustes
10. Evolução do produto

## Critérios de sucesso

- aplicação executa localmente sem erros;
- usuários conseguem criar conta e acessar o sistema;
- cadastro de produtos, clientes e encomendas funciona corretamente;
- estoque é controlado de forma confiável;
- testes automatizados cobrem a lógica principal;
- documentação está atualizada para onboarding e manutenção.
