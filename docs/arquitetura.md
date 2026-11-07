# Arquitetura

O sistema usa uma arquitetura monolítica em camadas:

- **Frontend:** React e TypeScript, responsável pela interface e consumo da API.
- **Controller:** valida requisições HTTP e expõe recursos REST.
- **Service:** concentra regras de negócio e coordena operações.
- **Repository:** realiza persistência por meio do Spring Data JPA.
- **PostgreSQL:** banco recomendado em produção; H2 é usado para desenvolvimento local.

## API principal

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/products` | Lista produtos e aceita `?search=` |
| GET | `/api/products/{id}` | Consulta um produto |
| POST | `/api/products` | Cadastra um produto |
| PUT | `/api/products/{id}` | Atualiza um produto |
| DELETE | `/api/products/{id}` | Remove um produto |
