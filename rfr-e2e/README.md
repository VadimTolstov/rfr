# rfr-e2e

E2E-тесты Rangiffler: JUnit Jupiter 6.1.3, Playwright Java 1.63, Allure 3 и Spring `JdbcClient`. Тестовые классы используют `@UsePlaywright(PlaywrightOptionsFactory.class)`; Playwright передаёт `Page` в параметры тестов.

## Запуск

```bash
./gradlew :rfr-e2e:installPlaywright
./gradlew :rfr-e2e:test
./gradlew :rfr-e2e:test --tests '*UserExtensionTest'
./gradlew :rfr-e2e:test --tests '*RegistrationTest'
./gradlew e2e
./gradlew :rfr-e2e:allureReport
```

Отчёт Allure: `rfr-e2e/build/reports/allure-report/allureReport/index.html`.

Задача `:rfr-e2e:test` по умолчанию исключает тесты с тегом `ui`. Если задан фильтр `--tests`, выбранный UI-тест запускается напрямую и требует работающего стенда.

Задача `e2e` запускает только UI-тесты. Перед запуском она ждёт до 180 секунд доступности MySQL `:3306`, `rfr-auth :9001`, `rfr-api :8081` и фронтенда `:3001`, затем проверяет миграции. Сервисы должны быть запущены заранее; задача ожидания не поднимает и не перезапускает их. Скрипт `scripts/wait-for-stand.sh` принимает `WAIT_TIMEOUT` для настройки общего таймаута.

На Windows Gradle выбирает Bash из `RFR_BASH`, затем системного свойства `rfr.bash`, затем Git Bash в стандартных каталогах. Если Bash не найден в этих местах, используется `bash` из `PATH`. В Linux используется `bash` из `PATH`.

## Конфигурация

`TestConfig` проверяет системное свойство, затем переменную окружения и затем значение по умолчанию. Gradle передаёт системные свойства `rfr.*` из JVM сборки в тестовую JVM. Например, `rfr.authUrl` соответствует `RFR_AUTH_URL`.

| Системное свойство | Переменная окружения | По умолчанию |
|---|---|---|
| `rfr.frontUrl` | `RFR_FRONT_URL` | `http://localhost:3001` |
| `rfr.authUrl` | `RFR_AUTH_URL` | `http://localhost:9001` |
| `rfr.apiUrl` | `RFR_API_URL` | `http://localhost:8081` |
| `rfr.dbUrl` | `RFR_DB_URL` | `jdbc:mysql://localhost:3306` |
| `rfr.dbUser` | `RFR_DB_USER` | `root` |
| `rfr.dbPassword` | `RFR_DB_PASSWORD` | `secret` |
| `rfr.migrationCheck` | `RFR_MIGRATION_CHECK` | `false` |
| `rfr.authMigrations` | `RFR_AUTH_MIGRATIONS` | `../rfr-auth/src/main/resources/db/migration/rangiffler-auth` |
| `rfr.apiMigrations` | `RFR_API_MIGRATIONS` | `../rfr-api/src/main/resources/db/migration/rangiffler-api` |
| `rfr.browser` | `RFR_BROWSER` | `chromium` |
| `rfr.headless` | `RFR_HEADLESS` | `true` |

`dbUrl` задаёт базовый JDBC URL; конфигурация добавляет к нему схему `rangiffler-auth` или `rangiffler-api` и `?serverTimezone=UTC`. При запуске через Gradle пути миграций по умолчанию передаются в тестовую JVM как абсолютные пути. `PlaywrightOptionsFactory` применяет настройки `browser` и `headless`; локаль контекста — `en-US`.

## Архитектура

Один source set `src/test`, пакет `io.student.rangiffler`:

| Пакет | Что лежит |
|---|---|
| `config` | `TestConfig` и `PlaywrightOptionsFactory` |
| `jupiter` | интеграция с JUnit: `annotation` (`@User`, `@ProbeTarget`), `extension` (`UserExtension`), `condition` (`ProbeTargetCondition`), `probe` (`UserExtensionTest`) |
| `data` | `TestUser`, `UserRequest`, `UserFixture`, `TearDown`, `SingleUserFixture` и декораторы `UserWithFriendsFixture`, `UserWithIncomeInvitations`, `UserWithOutcomeInvitationsFixture` |
| `data.db` | `Databases` с ленивыми Hikari-пулами (максимум четыре соединения на схему), `Jdbc`, модели и операции для профилей, учётных записей и связей |
| `utils` | `RandomDataUtils` — генерация тестовых имён и паролей |
| `ui` | `page` — Page Objects, `component` — общие части страниц |
| `test` | тесты в корневом пакете и по фичам: `auth`, `people`, `profile` |

Правила: в `jupiter` нет SQL и локаторов; в `data` нет JUnit; в `test` нет локаторов и SQL; `ThreadLocal` для состояния теста не используется — фикстуры хранятся в Store расширения.

## Агент

При изменении Page Objects следуй `.claude/skills/rfr-page-objects/SKILL.md`. UI-локаторы проверяются в браузере через Playwright MCP.

Подтверждение результата теста — запуск соответствующей Gradle-задачи.
