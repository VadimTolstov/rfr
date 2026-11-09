## Page Objects
При работе с Page Objects и UI-компонентами следуй `.claude/skills/rfr-page-objects/SKILL.md`.

## Запуск и проверка
Проход сценария через MCP не считается запуском E2E-теста.

```bash
./gradlew :rfr-e2e:test
./gradlew :rfr-e2e:test --tests '<Тест>'
./gradlew :rfr-e2e:installPlaywright
```

## Правила тестов
- JUnit Jupiter 6.1.3: `@ExtendWith(PageFixture.class)` + `@RegisterExtension UserExtension`.
- Данные — только `@User TestUser user` (БД, `CloseableResource`), не хардкод.
- Fluent-цепочка, без `Allure.step` вокруг всего теста.
- `@DisplayName` для названий.
## Запрещено
Хардкодить URL, пароли или тестовые данные — всё из `TestConfig` или фикстур.

## Специфика проекта
- Стенд: MySQL `3306`, auth `9001`, API `8081`, фронт `3001`.
- `--rerun-tasks` не нужен (`test` настроен с `outputs.upToDateWhen { false }`).
- Проверка компиляции — `check-code.bat` (см. корневой `AGENTS.md`).
