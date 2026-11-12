# Arquitetura

O sistema usa uma arquitetura monolítica em camadas:

- **Frontend:** React e TypeScript, responsável pela interface e consumo da API.
- **Controller:** valida requisições HTTP e expõe recursos REST.
- **Service:** concentra regras de negócio e coordena operações.
- **Repository:** realiza persistência por meio do Spring Data JPA.
- **Banco de dados:** PostgreSQL é o banco padrão no desenvolvimento local e na produção; H2 é usado somente nos testes automatizados.

## API principal

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/products` | Lista produtos e aceita `?search=` |
| GET | `/api/products/{id}` | Consulta um produto |
| POST | `/api/products` | Cadastra um produto |
| PUT | `/api/products/{id}` | Atualiza um produto |
| DELETE | `/api/products/{id}` | Remove um produto |
| GET | `/api/dashboard/notes` | Consulta os lembretes privados da conta autenticada |
| PUT | `/api/dashboard/notes` | Atualiza os lembretes (até 5.000 caracteres) |
| DELETE | `/api/dashboard/notes` | Exclui o bloco de lembretes da conta autenticada |
| GET | `/api/sales` | Lista as vendas da conta autenticada |
| POST | `/api/sales` | Registra uma venda e atualiza o estoque |

O dashboard frontend agrega os dados de produtos, clientes, encomendas e vendas para exibir os totais da conta. Os lembretes são persistidos no perfil da conta e não são compartilhados entre usuários.
