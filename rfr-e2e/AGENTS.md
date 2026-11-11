## Page Objects
При работе с Page Objects и UI-компонентами следуй `.claude/skills/rfr-page-objects/SKILL.md`.

## Запуск и проверка
Проход сценария через MCP не считается запуском E2E-теста.

```bash
./gradlew :rfr-e2e:test
./gradlew :rfr-e2e:test --tests '<Тест>'
./gradlew e2e
./gradlew :rfr-e2e:installPlaywright
```

`:rfr-e2e:test` исключает тесты с тегом `ui`, если запуск не ограничен фильтром `--tests`. `e2e` ждёт доступности сервисов, проверяет миграции и запускает только тесты с тегом `ui`; ожидание не поднимает сервисы.

`scripts/wait-for-stand.sh` проверяет MySQL, auth, API и фронтенд. Таймаут по умолчанию — 180 секунд. На Windows Bash выбирается из `RFR_BASH`, системного свойства `rfr.bash`, стандартных каталогов Git Bash или `PATH`.

## Правила тестов
- На классах тестов используй `@UsePlaywright(PlaywrightOptionsFactory.class)`; Playwright предоставляет `Page` как параметр теста.
- Аннотация параметра `@User TestUser user` подключает `UserExtension` и создаёт тестовые данные через `UserRequest` и композицию `UserFixture`.
- `UserFixture` реализует `AutoCloseable`; JUnit закрывает фикстуру из Store благодаря `junit.jupiter.extensions.store.close.autocloseable.enabled=true` в `junit-platform.properties`.
- Данные создавай через `@User`, не хардкодь пользователей или связи.
- Fluent-цепочка, без `Allure.step` вокруг всего теста.
- `@DisplayName` для названий.
## Запрещено
Хардкодить URL, пароли или тестовые данные — всё из `TestConfig` или фикстур.

## Специфика проекта
- Стенд: MySQL `3306`, auth `9001`, API `8081`, фронт `3001`.
- `--rerun-tasks` не нужен (`test` настроен с `outputs.upToDateWhen { false }`).
- Проверка компиляции — `check-code.bat` (см. корневой `AGENTS.md`).
