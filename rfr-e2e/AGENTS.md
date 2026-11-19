# AGENTS.md — rfr-e2e

## Навыки

- Для Page Objects и UI-компонентов применяй `rfr-page-objects`.
- Для тестовых пользователей, API-профилей и связей применяй `rfr-test-data`.
- Для ревью фикстур, JUnit extensions и владения ресурсами применяй `rfr-test-review`.
- `unit-tests` применяй к изолированным unit-тестам; для `EngineTestKit`, HTTP-проб, MySQL и жизненного цикла JUnit приоритет имеют предметные требования `rfr-test-review`.
- В Codex проектные навыки находятся в `.agents/skills/<name>/SKILL.md`; для Claude Code сохраняются одинаковые копии в `.claude/skills/<name>/SKILL.md`. При изменении навыка синхронизируй обе копии.
- Сначала читай глобальные правила, корневой `AGENTS.md`, этот файл и относящиеся к задаче навыки. Не создавай исключений из глобальных строгих правил лишь из-за привычного стиля проекта.

## Page Objects

При работе с Page Objects и UI-компонентами следуй `rfr-page-objects`. Доступные имена и роли элементов подтверждай через Playwright MCP по текущим адресам `TestConfig`. Проход сценария через MCP не считается запуском E2E-теста.

## Запуск и проверка

```bash
./gradlew :rfr-e2e:test
./gradlew :rfr-e2e:test --tests '<Тест>'
./gradlew e2e
./gradlew :rfr-e2e:installPlaywright
```

На Windows используй соответствующие команды `gradlew.bat`. Перед запуском тестов получай разрешение согласно глобальному `AGENTS.md`, если оно ещё не дано текущим запросом. Не поднимай Docker, серверы или фронтенд автоматически без разрешения.

`:rfr-e2e:test` по умолчанию исключает классы с тегом `ui`, если фильтр `--tests` не изменяет состав выборки. Самостоятельные тесты фикстур при этом могут требовать MySQL. `e2e` запускает UI-тесты, ожидает доступности сервисов и сверяет миграции при соответствующих флагах. Ожидание не поднимает сервисы; Bash-задача `waitForStand` остаётся отдельной ручной возможностью.

`check-code.bat` проверяет компиляцию затронутых модулей и не заменяет запуск тестов. Итоговый отчёт должен отделять результаты Gradle от просмотра кода и Playwright MCP.

## Глобальные расширения JUnit

В `src/test/resources/META-INF/services/org.junit.jupiter.api.extension.Extension` сохраняй порядок:

1. `AllureEnvironmentExtension` — регистрирует публикацию `environment.properties` до возможной ошибки подготовки.
2. `DatabasePoolsExtension` — регистрирует один `PoolLease` на корневой контекст до Readiness и Schema, не создавая подключения к MySQL.
3. `StandReadinessExtension` — перед первым UI-классом ожидает MySQL, auth, API и фронтенд до `rfr.standTimeout` (по умолчанию 180 секунд), только при включённом `rfr.standCheck`.
4. `StandSchemaExtension` — после успешного readiness сверяет миграции для UI-класса, только при включённом `rfr.migrationCheck`.

- Реализации `SuiteExtension` регистрируются только глобально через ServiceLoader. Исключение: подставные расширения в мишенях `@ProbeTarget`, тестирующих контракт через `EngineTestKit`.
- `DatabasePoolsExtension` реализует `BeforeAllCallback` напрямую и тоже зарегистрирован глобально. Не связывай закрытие HikariCP с `AllureEnvironmentExtension` или проверкой миграций.
- `Databases.AUTH` и `Databases.API` создают общие ленивые пулы. `DatabasePoolLifecycle` освобождает их через `TearDown` после закрытия последнего активного `PoolLease`, включая параллельные и вложенные запуски.
- Если вложенный `EngineTestKit` использует общие `Databases` и ограничивает `junit.jupiter.extensions.autodetection.include`, он обязан включать `DatabasePoolsExtension`. В противном случае такой запуск не учитывается среди владельцев пулов.
- `@ProbeTarget` не должен выполняться как обычный тест Gradle вне разрешающего режима `ProbeTargetCondition`.
- `junit-platform.properties` должен обеспечивать автозагрузку расширений и закрытие `AutoCloseable` из Store.

## Флаги глобальных расширений

| Свойство | Назначение | По умолчанию в `test` и прямом JUnit-запуске из IDE | В `e2e` |
|---|---|---|---|
| `rfr.standCheck` | Ожидание готовности сервисов | `false` | `true` |
| `rfr.migrationCheck` | Сверка миграций | `false` | `true` |
| `rfr.environmentDb` | Чтение версий схем для Allure | `false` | `true` |

Для ожидания при UI-запуске из IDE укажи `-Drfr.standCheck=true`. Для сверки миграций добавь `-Drfr.migrationCheck=true` и корректные пути миграций, для чтения версий в отчёт — `-Drfr.environmentDb=true`. Значения по умолчанию действуют, если свойства и переменные окружения не переопределены.

При выключенном `rfr.environmentDb` Allure сохраняет `environment.properties` со всеми ключами и версиями `unavailable`, не обращаясь к MySQL ради чтения версий. При включённом чтении недоступная БД не должна препятствовать сохранению файла. Эти флаги управляют глобальными расширениями, но не исключают обращения к БД из самостоятельных тестов пользовательских фикстур.

## Правила тестов

- Для UI-классов используй `@UsePlaywright(BrowserOptions.class)`; `Page` передаётся как параметр теста.
- `@User TestUser user` использует `UserRequest`, композицию `UserFixture` и очистку через JUnit Store; не создавай те же записи вручную.
- `UserFixture` реализует `AutoCloseable`; JUnit закрывает ресурсы Store благодаря `junit.jupiter.extensions.store.close.autocloseable.enabled=true`.
- Пользователей и связи создавай через фикстуры, не хардкодь логины, пароли, URL и тестовые данные.
- В Allure используй fluent-цепочки Page Objects без оборачивания целого тестового метода в `Allure.step`.
- Используй `@DisplayName` для названий тестов. Не включай секреты в шаги Allure, параметры отчёта или диагностические сообщения.
- `@BeforeEach` и `@AfterEach` допустимы для простой независимой подготовки и очистки по глобальным правилам; для общих ресурсов и сложного жизненного цикла предпочитай JUnit Extensions.
- Проверяй наблюдаемые результаты и публичные контракты, включая `HikariDataSource.isClosed()` при тестировании освобождения ресурсов; не проверяй приватное состояние ради привязки к реализации.

## Специфика проекта

- Стенд: MySQL `3306`, auth `9001`, API `8081`, фронт `3001`.
- `--rerun-tasks` не нужен (`test` настроен с `outputs.upToDateWhen { false }`).
- Для компиляции затронутых модулей используй `check-code.bat` (см. корневой `AGENTS.md`).
