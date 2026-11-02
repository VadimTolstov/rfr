## Запуск
Нужны `rfr-auth` (`9001`), `rfr-api` (`8081`); фронт работает на `3001`.

```bash
cd rfr-gql-client
npm ci
npm run dev
```

## Интеграционные контракты
- Порт `3001` используется в OAuth redirect URI; не меняй его отдельно.
- В `rfr-api` отправляй только `access_token`, не `id_token`.
- Ответ Apollo mutation и snackbar не доказывают сохранение: проверь независимым чтением.
- Маршруты: `/` (Login/Register), `/my-travels`, `/people`, `/profile` (protected).
- Каждая GraphQL-операция — отдельный хук в `src/hooks` с постоянным `operationName`.
