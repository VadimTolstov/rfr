# rfr-e2e

E2E-тесты Rangiffler: JUnit Jupiter 6.1.3, Playwright Java 1.63, Allure 3 и Spring `JdbcClient`. Тестовые классы используют `@UsePlaywright(PlaywrightOptionsFactory.class)`; Playwright передаёт `Page` в параметры тестов.

## Запуск

```bash
./gradlew :rfr-e2e:installPlaywright                      # один раз: браузеры Playwright
./gradlew :rfr-e2e:test --tests '*RegistrationTest'
./gradlew :rfr-e2e:test --tests '*MainPageTest'
./gradlew :rfr-e2e:test --tests '*FriendsTabTest' --tests '*IncomeInvitationsTabTest' --tests '*OutcomeInvitationsTabTest'
./gradlew :rfr-e2e:test --tests '*UserExtensionTest'
./gradlew :rfr-e2e:allureReport                           # rfr-e2e/build/reports/allure-report/allureReport/index.html
```

Стенд: MySQL `:3306`, `rfr-auth :9001`, `rfr-api :8081`, фронт `:3001` (см. корневой README).

## Конфигурация

`TestConfig` проверяет системное свойство тестовой JVM, затем переменную окружения и затем значение по умолчанию. Для запуска через Gradle используй переменные окружения: параметры `-Drfr.*`, переданные Gradle, не настроены на передачу в тестовую JVM. Например, `rfr.authDbUrl` соответствует `RFR_AUTH_DB_URL`.

| Ключ | По умолчанию |
|---|---|
| `rfr.frontUrl` | `http://localhost:3001` |
| `rfr.authUrl` | `http://localhost:9001` |
| `rfr.apiUrl` | `http://localhost:8081` |
| `rfr.authDbUrl` | `jdbc:mysql://localhost:3306/rangiffler-auth` |
| `rfr.apiDbUrl` | `jdbc:mysql://localhost:3306/rangiffler-api` |
| `rfr.dbUser` / `rfr.dbPassword` | `root` / `secret` |

Браузерные параметры не читаются из `TestConfig`: `PlaywrightOptionsFactory` включает headless-режим и задаёт локаль `en-US`.

## Архитектура

Один source set `src/test`, пакет `io.student.rangiffler`:

| Пакет | Что лежит |
|---|---|
| `config` | `TestConfig` и `PlaywrightOptionsFactory` |
| `jupiter` | интеграция с JUnit: `annotation` (`@User`, `@ProbeTarget`), `extension` (`UserExtension`), `condition` (`ProbeTargetCondition`), `probe` (`UserExtensionTest`) |
| `data` | `TestUser`, `UserRequest`, `UserFixture`, `TearDown`, `SingleUserFixture` и декораторы `UserWithFriendsFixture`, `UserWithIncomeInvitations`, `UserWithOutcomeInvitationsFixture` |
| `data.db` | `Databases`, `Jdbc`, а также модели и операции для профилей, учётных записей и связей |
| `utils` | `RandomDataUtils` — генерация тестовых имён и паролей |
| `ui` | `page` — Page Objects, `component` — общие части страниц |
| `test` | тесты в корневом пакете и по фичам: `auth`, `people`, `profile` |

Правила: в `jupiter` нет SQL и локаторов; в `data` нет JUnit; в `test` нет локаторов и SQL; `ThreadLocal` для состояния теста не используется — фикстуры хранятся в Store расширения.

## Агент

При изменении Page Objects следуй `.claude/skills/rfr-page-objects/SKILL.md`. UI-локаторы проверяются в браузере через Playwright MCP.

Подтверждение результата теста — запуск соответствующей Gradle-задачи.
