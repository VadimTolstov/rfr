## Навык тестов
При написании юнит-тестов следуй правилам из `.claude/skills/unit-tests/SKILL.md`.

## Запуск

```bash
./gradlew :rfr-auth:bootRun
curl http://localhost:9001/.well-known/openid-configuration
```

## Нюансы
- Регистрация создаёт учётную запись в `rangiffler-auth`; профиль API появляется при первом `Query.user`.
- `POST /register` требует CSRF-токен той же сессии: сначала выполни `GET /register`.
- `GET /login` без сохранённого OAuth-запроса перенаправляет на фронтенд; вход начинай с `/oauth2/authorize`.
- RSA-ключ подписи генерируется при каждом запуске; выданные токены после перезапуска невалидны.
- OAuth-авторизации и сессии хранятся в памяти и теряются при перезапуске.
- Клиент `client` публичный, без секрета; refresh token не выдаётся.
