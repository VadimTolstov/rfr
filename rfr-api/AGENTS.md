## Навык тестов
При написании юнит-тестов следуй правилам из `.claude/skills/unit-tests/SKILL.md`.

## Запуск
GraphQL endpoint: `http://localhost:8081/graphql`.
Для проверки JWKS должен быть запущен `rfr-auth`.
```bash
./gradlew :rfr-api:bootRun
```

## Что mock, а что реально
- Реальное (БД): профиль, страны, люди, дружба.
- Mock (JSON): лента, статистика, фото, лайки — ничего не сохраняется.
- Таблицы `photo`, `like`, `photo_like`, `statistic` в миграции есть, но не используются.
- Не заменяй текущие mock-реализации без явного указания.
- Профиль создаётся при первом `Query.user` со страной `ru`.
- Пользователи `rangiffler-api` и `rangiffler-auth` связаны по username, UUID у них разные.
- GraphQL-ошибки приходят с HTTP 200; тип находится в `extensions.classification`.
- Query требуют `read`, mutation — `write`; принимается только access token, `id_token` отклоняется с 401.
- Дружба — одна строка на пару: `ACCEPT`/`REJECT` доступны только адресату, `DELETE` — любой стороне.
- Перед удалением пользователя вручную удаляются его строки `friendship` (каскада нет).
