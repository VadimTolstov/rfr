## Назначение

GraphQL API (`POST http://localhost:8081/graphql`), OAuth 2.0 Resource Server. Подробности — `rfr-api/README.md`.

## 🧠 Обязательный Skill для тестов
При написании юнит-тестов **обязательно** следуй правилам из `.claude/skills/unit-tests/SKILL.md`.

## Запуск

```bash
./gradlew :rfr-api:bootRun
```

Нужен запущенный `rfr-auth`: API проверяет JWT по его JWKS.

## Что настоящее, а что мок

| Возможность | Реализация |
|---|---|
| Профиль (`Query.user`, `Mutation.user`), страны, люди, дружба | БД `rangiffler-api` |
| Лента и статистика (`Query.feed`) | JSON из `src/main/resources/mock/`, одинаковый для всех |
| Создание, редактирование, лайк и удаление фото | Мок: ответ корректный, но ничего не сохраняется |

Таблицы `photo`, `like`, `photo_like`, `statistic` есть в миграции, но приложение их не использует.
Моки должны оставаться в проекте до явного указания на необходимость их замены на реальные реализации.

## Что важно знать

- Схема — `src/main/resources/graphql/query.graphqls`. Java-модель генерирует DGS Codegen (`./gradlew :rfr-api:generateJava`) в `build/generated/sources/dgs-codegen`, руками её не пишут.
- Профиль создаётся при первом `Query.user` со страной `ru`.
- Пользователи `rangiffler-api` и `rangiffler-auth` связаны только по username, UUID у них разные.
- Ошибки приходят с HTTP 200 в `errors`; тип — в `extensions.classification` (`BAD_REQUEST`, `NOT_FOUND`, `UNAUTHORIZED`, `FORBIDDEN`).
- Query требуют authority `read`, mutation — `write`. Принимается только access token, `id_token` отклоняется с 401.
- Дружба — одна строка на пару в `friendship`. `ADD` создаёт `PENDING`, `ACCEPT` и `REJECT` доступны только адресату, `DELETE` — любой стороне.
- Внешних ключей с каскадным удалением нет: перед удалением пользователя удаляются его строки в `friendship`.