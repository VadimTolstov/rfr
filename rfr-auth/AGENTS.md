## Назначение

Spring Authorization Server (OAuth 2.1 / OIDC): регистрация, вход, выдача токенов фронтенду по Authorization Code + PKCE. Подробности — `rfr-auth/README.md`.

## 🧠 Обязательный Skill для тестов
При написании юнит-тестов **обязательно** следуй правилам из `.claude/skills/unit-tests/SKILL.md`.

## Запуск

```bash
./gradlew :rfr-auth:bootRun
curl http://localhost:9001/.well-known/openid-configuration
```

## Что важно знать

- Регистрация создаёт пользователя только в схеме `rangiffler-auth` (таблицы `user` и `authority` с `read`, `write`). Профиль в `rangiffler-api` появляется при первом `Query.user`.
- `POST /register` требует CSRF-токен той же сессии: сначала `GET /register`. Успех — HTTP 201, ошибка валидации или занятый username — HTTP 400.
- Правила формы: `username` 3–50 символов, `password` 3–12 символов, оба без whitespace, `passwordSubmit` совпадает с `password`.
- `GET /login` без сохранённого OAuth-запроса уводит на фронтенд. Вход начинается с `/oauth2/authorize` или кнопки Login на фронтенде.
- Ключ подписи RSA генерируется при каждом старте. После перезапуска все выданные токены невалидны.
- OAuth-авторизации и сессии хранятся в памяти и теряются при перезапуске.
- Публичный клиент `client` без секрета, refresh token не выдаётся.