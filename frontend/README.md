# Frontend — Mãos Conectadas

Interface web em React, TypeScript e Vite.

## Requisitos

- Node.js 20 ou superior
- npm

## Desenvolvimento

Na pasta `frontend`, instale as dependências e inicie o servidor:

```powershell
npm install
npm run dev -- --host 0.0.0.0
```

A aplicação fica disponível em `http://localhost:5173`. Durante o desenvolvimento, as requisições para `/api` são encaminhadas ao backend em `http://localhost:8080`.

Para usar outro backend com o proxy do Vite, defina `VITE_BACKEND_URL` antes de iniciar o servidor:

```powershell
$env:VITE_BACKEND_URL="http://localhost:8080"
npm run dev -- --host 0.0.0.0
```

Também é possível configurar `VITE_API_URL` para apontar o cliente diretamente a uma URL de API, sem usar o proxy.

## Validação

```powershell
npm run lint
npm run build
```
