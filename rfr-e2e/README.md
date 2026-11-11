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

Задача `e2e` запускает UI-тесты через `StandSuiteTest` (`@Suite`, `@IncludeTags("ui")`). Внешний движок Gradle ограничен `junit-platform-suite`, внутри suite используется `junit-jupiter`. Сверка миграций выполняется в `@BeforeSuite`, если включён `rfr.migrationCheck` (задача `e2e` включает его). Обычная задача `test` использует только `junit-jupiter`. Перед запуском она ждёт до 180 секунд доступности MySQL `:3306`, `rfr-auth :9001`, `rfr-api :8081` и фронтенда `:3001`, затем проверяет миграции. Сервисы должны быть запущены заранее; задача ожидания не поднимает и не перезапускает их. Скрипт `scripts/wait-for-stand.sh` принимает `WAIT_TIMEOUT` для настройки общего таймаута.

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

## Проверка JUnit Suite (2026-11-11)

Проверено на работающем стенде, JDK 25, Gradle 9.7.1, JUnit 6.1.3, Allure Java 3.0.0 и Allure Report 3.17.0. Команды ниже записаны в Unix-форме; на Windows использовался `gradlew.bat`.

| Запуск | `@BeforeSuite` | Фактический результат |
|---|---|---|
| `./gradlew e2e` | Да, перед UI-тестами | 16 UI-тестов прошли, каждый выполнен один раз |
| `./gradlew e2e` без ограничения внешних движков | Да для suite; прямой Jupiter обходит suite | Каждый из 16 UI-тестов выполнен дважды (32 выполнения); дополнительно обнаружены проверки расширений и демонстрационные тесты |
| `./gradlew e2e --tests '*TabTest'` | Нет | Gradle завершается до запуска: `Problem configuring task :e2e from command line. Unknown command-line option '--tests'` |
| `./gradlew :rfr-e2e:e2e --tests '*TabTest'` | Да | 6 тестов трёх вкладок прошли через suite; неверный путь миграций приводит к `initializationError` |
| `./gradlew :rfr-e2e:test --tests '*FriendsTabTest'` | Нет | Оба теста прошли даже с `rfr.migrationCheck=true` и отсутствующим каталогом миграций |
| `./gradlew :rfr-e2e:test --tests '*FriendsTabTest.shouldShowEmptyState'` | Нет | Один тест прошёл с теми же диагностическими свойствами |
| Один класс или метод непосредственно через JUnit в IDE | По устройству JUnit — нет: suite не выбран | Фактический запуск IDE не подтверждён: MCP-запросы обнаружения точек запуска и запуска класса завершились таймаутами по 300 секунд; прямые Gradle-запуски приведены отдельно и не считаются проверкой IDE |

Корневая `e2e` — задача-обёртка, а `:rfr-e2e:e2e` — задача типа `Test`. Поэтому для фильтрации нужно указывать модульную задачу. При прямом запуске класса или метода проверка миграций больше не подключается через ServiceLoader, даже если включено её свойство. При запуске suite непосредственно из IDE для сверки нужно включить `-Drfr.migrationCheck=true` и задать корректные пути миграций относительно рабочей директории IDE.

### Падение перед suite

Проверка выполнялась с `-Drfr.authMigrations=<абсолютный путь к отсутствующему каталогу>`: база не изменялась. В выводе Gradle:

```text
StandSuiteTest > initializationError FAILED
    java.lang.IllegalStateException at StandSuiteTest.java:29

1 test completed, 1 failed
> Task :rfr-e2e:e2e FAILED
BUILD FAILED
```

Код завершения Gradle — 1. XML содержит синтетический тест `initializationError`, а HTML-отчёт Gradle показывает ошибку и стек. Тела UI-тестов не выполняются.

Allure Java создаёт пустой `*-container.json` и `*-globals.json` с глобальной ошибкой:

```text
Container StandSuiteTest failed, tests inside it did not run: Migrations directory ... of schema rangiffler-auth does not exist
```

В глобальной ошибке есть стек с `StandSuiteTest.validateMigrations`. Файлов `*-result.json` отдельных тестов нет. В сгенерированном Allure 3 ошибка сохранена в `widgets/globals.json`; в `summary.json` при этом `stats.total=0` и `status="passed"`. Следовательно, общий статус Allure не заменяет код завершения Gradle: ошибка запуска представлена глобально, а не как упавший UI-тест.

### Артефакты проверки

Локальные логи и XML сохранены в `build/verification/`, первый успешный лог — `build/suite-only.log`. Отдельные каталоги Allure для контрольных запусков: `suite-isolated/allure-results`, `unrestricted-isolated/allure-results`, `failure-isolated/allure-results`, `direct-class/allure-results`, `direct-method/allure-results`, `unit/allure-results` внутри `build/verification/`. Отчёт по ошибке: `build/verification/failure-isolated/report/index.html`.

Временный `build/verification/probe.init.gradle` снимает ограничение движков только при `-Dprobe.unrestricted=true` и задаёт отдельный каталог Allure через JVM argument provider. Это необходимо, поскольку плагин Allure переопределяет обычное системное свойство каталога результатов. Пример повторения эксперимента:

```bash
./gradlew e2e -I rfr-e2e/build/verification/probe.init.gradle -Dprobe.case=another-run -Dprobe.unrestricted=true
```

Вспомогательный скрипт и отчёты находятся в `build/` и не входят в исходный код. Настройка `build.gradle` остаётся ограниченной suite-движком. `check-code.bat` и `:rfr-e2e:test` прошли успешно.