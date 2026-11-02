## Назначение

SPA на React 19 + TypeScript, Apollo Client 4, MUI 9, React Router. Подробности — `rfr-gql-client/README.md`.

## Запуск

Собирается отдельно от Gradle. Нужны запущенные `rfr-auth` (9001) и `rfr-api` (8081).

```bash
cd rfr-gql-client
npm ci
npm run dev     # http://localhost:3001
npm run build   # tsc + vite build
npm run lint    # eslint, --max-warnings 0
```

## Что важно знать

- Порт 3001 зашит в redirect URI клиента в `rfr-auth`, менять его нельзя.
- Маршруты: `/` (Login и Register), `/my-travels`, `/people`, `/profile` (защищённые, через `PrivateRoute`).
- Каждая GraphQL-операция — отдельный хук в `src/hooks` с постоянным `operationName` (`GetUser`, `GetFeed`, `UpdateUser`, `FriendshipAction` и т. д.). Новая операция — новый хук.
- Токены `access_token` и `id_token` лежат в localStorage. В `rfr-api` уходит только access token.
- Кэш Apollo сразу обновляет карточку ответом мутации. Успешный snackbar не доказывает сохранение: проверяй повторным чтением.
- Компоненты берутся из MUI, свои с нуля не пишутся.