# rfr-e2e

E2E-тесты Rangiffler: JUnit Jupiter 6.1.3, Playwright Java 1.63, Allure 3 и Spring `JdbcClient`. Тестовые классы используют `@UsePlaywright(PlaywrightOptionsFactory.class)`; Playwright передаёт `Page` в параметры тестов.

## Запуск

```bash
./gradlew :rfr-e2e:installPlaywright
./gradlew :rfr-e2e:test
./gradlew :rfr-e2e:test --tests '*UserExtensionTest'
./gradlew :rfr-e2e:test --tests '*RegistrationTest'
./gradlew e2e
./gradlew e2e --tests '*TabTest'
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

## SuiteExtension / @BeforeSuite / LauncherSessionListener

| Подход | Когда срабатывает | Запуск класса или метода из IDE | Store | Теги | Как падает |
|---|---|---|---|---|---|
| `SuiteExtension` | Перед первым выбранным Jupiter-классом, один раз на реализацию в корневом контексте; внутри `EngineTestKit` — отдельный корень | Да, если расширение подключено через ServiceLoader с включённым autodetection или через `@ExtendWith` | Получает корневой `ExtensionContext.Store`; `SuiteRun` сохраняет состояние и закрывает ресурсы | Получает `ExtensionContext`, может читать теги контекста; корневые теги не являются объединением тегов всех тестов | Ошибка контейнеров классов; `SuiteRun` сохраняет ошибку и повторно бросает её для остальных классов, тела тестов не запускаются |
| `@BeforeSuite` | Перед выполнением выбранного класса `@Suite`; без ограничения движков UI может дополнительно запускаться напрямую через Jupiter | Только при запуске suite; прямой запуск отдельного тестового класса или метода обходит её | Нет Jupiter `ExtensionContext.Store` | Состав suite задаётся аннотациями отбора; callback не получает контекст Jupiter с тегами выбранного теста | Gradle показывает `StandSuiteTest > initializationError FAILED`; Allure 3 сохраняет глобальную ошибку контейнера без результатов UI-тестов |
| `LauncherSessionListener` | При открытии каждой launcher-сессии, до discovery и фильтрации; в Gradle — по одному вызову на фактически запущенный тестовый процесс в проверенной конфигурации | Да, при автозагрузке слушателя launcher-ом; класс и метод проверены в IDE через MCP `idea` | Нет `ExtensionContext.Store`, но в JUnit 6.1.3 есть собственный `LauncherSession.getStore()` с закрытием `AutoCloseable` при закрытии сессии | В `launcherSessionOpened` ещё нет обнаруженного плана и тегов выбранных тестов | Ошибка открытия сессии: Gradle сообщает `Could not start Gradle Test Executor`, JUnit runner IntelliJ — `Internal Error occurred.`, до начала отдельных тестов |

`SuiteExtension` остаётся для поведения, которому нужны корневой контекст Jupiter, его Store и работа внутри `EngineTestKit`, а сверка миграций в этой попытке перенесена в `LauncherSessionListener` для проверки до обнаружения тестов.

### Реализация слушателя

`StandSchemaExtension` реализует `LauncherSessionListener`, выполняет прежнюю сверку auth/API в `launcherSessionOpened` и зарегистрирован только в `META-INF/services/org.junit.platform.launcher.LauncherSessionListener`. Его регистрация в `org.junit.jupiter.api.extension.Extension` убрана. Зависимость `junit-platform-launcher` переведена в `testImplementation`.

Условие `TestConfig.migrationCheck()` сохранено: `e2e` включает проверку, `:rfr-e2e:test` по умолчанию её пропускает. Вызов слушателя и запуск сверки — разные события. Перед условием слушатель пишет сообщение `Launcher session opened for migration validation, pid=..., migrationCheck=...`; оно позволяет считать вызовы даже при отключённой сверке, не ориентируясь на подключение к MySQL. Значения `migrationCheck` и PID не содержат учётных данных.

В JUnit 6.1.3 у сессии есть собственный Store; его нельзя смешивать со Store расширений Jupiter. См. [LauncherSession API](https://docs.junit.org/6.1.3/api/org.junit.platform.launcher/org/junit/platform/launcher/LauncherSession.html).

### Фактические проверки 2026-11-11

Проверено с JDK 25, Gradle 9.7.1, JUnit 6.1.3 и IntelliJ IDEA 2026.2.1. Команды ниже приведены в Unix-форме; на Windows использовался `gradlew.bat`.

| Запуск | Наблюдение | Результат |
|---|---|---|
| `./gradlew :rfr-e2e:test` | Один маркер, PID `32736`, `migrationCheck=false`; UI исключены настройкой задачи | Успех: слушатель срабатывает даже без UI, сверка базы не выполняется |
| Та же задача с `maxParallelForks=2` | Два маркера с PID `14908` и `1720`, оба с `migrationCheck=false` | Успех: два фактически запущенных процесса, по одной сессии в каждом |
| `./gradlew :rfr-e2e:test --tests '*SuiteExtensionTest'` | Один маркер внешней сессии, PID `33216`; все 22 проверки прошли, включая повторные внутренние запуски | `EngineTestKit.engine("junit-jupiter").execute()` новых launcher-сессий не создаёт и слушатель внутри себя не вызывает |
| `./gradlew e2e` | Один маркер, PID `5032`, `migrationCheck=true` | Сверка обеих баз и все 16 UI-тестов прошли |
| `:rfr-e2e:test` с включённой сверкой и отсутствующим каталогом миграций | Маркер `migrationCheck=true`, затем исключение из `Migrations.mismatch` через `launcherSessionOpened` | Gradle завершается с кодом 1 до выполнения тестов |
| JUnit-конфигурация в IDE через MCP `idea`, `SuiteExtensionTest`, сверка отключена | Один маркер, PID `40992`; 22 события `testFinished`, ошибок нет | 22 теста прошли, код завершения 0 |
| JUnit-конфигурация в IDE через MCP `idea`, метод `beforeSuiteRunsOnceForSeveralClasses`, сверка отключена | Один маркер, PID `8224`; одно событие `testFinished` | Один тест прошёл, код завершения 0 |
| JUnit-конфигурация в IDE через MCP `idea`, включённая сверка и отсутствующий каталог миграций | Один маркер, PID `40536`; `Internal Error occurred.` и стек исключения | Код завершения `-2`; событий начала отдельных тестов нет |

`maxParallelForks=2` — верхний предел, а не гарантия двух процессов: наблюдаемые два вызова относятся к полному набору этой задачи, без `forkEvery`. Ограничение только одним классом, дополнительные задачи, перезапуски и другие launcher-сессии могут изменить число вызовов. Параллелизм Jupiter внутри процесса не создаёт дополнительные сессии.

Для эксперимента с двумя процессами использовался временный init-скрипт в `build/verification/launcher-session/probe.init.gradle`, задающий `maxParallelForks=2` через `-Dprobe.forks=2` и включающий `testLogging.showStandardStreams=true`. Постоянная настройка числа процессов в сборке не менялась. Аналогичный минимальный скрипт для повторения:

```groovy
gradle.projectsEvaluated {
    rootProject.project(':rfr-e2e').tasks.named('test') {
        maxParallelForks = 2
        testLogging.showStandardStreams = true
    }
}
```

Сохранить его в `rfr-e2e/build/two-forks.init.gradle` и запустить `./gradlew :rfr-e2e:test -I rfr-e2e/build/two-forks.init.gradle --info`. Для подсчёта искать строки сообщения, а не упоминания имени метода в стеке исключения. Внутренние вызовы `EngineTestKit` отличаются от внешнего запуска тем, что не дают дополнительных маркеров: при 22 проверках остаётся один маркер внешней сессии.

### Исключение до обнаружения тестов

Ошибка воспроизводится без изменения базы:

```bash
./gradlew :rfr-e2e:test -Drfr.migrationCheck=true -Drfr.authMigrations=/absolute/path/to/missing-migrations
```

Каталог должен действительно отсутствовать. Ожидаемый вывод Gradle, подтверждённый в проверке:

```text
Gradle Test Executor 7 FAILED
    org.gradle.api.internal.tasks.testing.TestSuiteExecutionException at SuiteTestDefinitionProcessor.java:45
        Caused by: java.lang.IllegalStateException at Migrations.java:29

Execution failed for task ':rfr-e2e:test'
> Test process encountered an unexpected problem.
   > Could not start Gradle Test Executor 7.
      > Migrations directory ... of schema rangiffler-auth does not exist
BUILD FAILED
```

Это ошибка запуска тестового процесса, а не упавшая проверка внутри тестового метода. В JUnit runner IntelliJ вывод начинается с `Internal Error occurred.`, затем идёт исходный `IllegalStateException`; стек включает `DefaultLauncherSession`, `SessionPerRequestLauncher` и `JUnit6TestRunnerHelper`. Нет событий `testStarted`/`testFailed` для отдельных тестов.

Проверка в IDE выполнена через другой MCP — `idea`: `get_run_configurations` обнаружил точки запуска, `execute_run_configuration` запустил временные конфигурации типа `JUnit` на модуле `rfr.rfr-e2e.test`. Подтверждены успешные запуски класса (22 теста) и метода (один тест), а также исключение из слушателя при `-Drfr.migrationCheck=true` и отсутствующем каталоге. Консоль ошибки получена непосредственно из IDE. Предыдущая проверка через `amplicode` завершалась таймаутом; прямой запуск `JUnitStarter` теперь дополнен проверкой через IDE. Существующие конфигурации запуска класса и метода в проекте имеют тип `Gradle build`: запуск класса прошёл, а конфигурация метода выбирает `:rfr-e2e:e2e` и не находит этот тест из-за фильтра `ui`. Их настройки не менялись. Временные JUnit-конфигурации после проверки переименованы в `.bak`. При делегировании запуска из IDE в Gradle действует поведение Gradle.

Локальные логи: `build/verification/launcher-session/{unit,two-forks,engine-testkit,ui,failure,idea-runner-success,idea-runner-failure,idea-mcp-class,idea-mcp-method,idea-mcp-failure}.log`; результаты Allure изолированы в подкаталогах экспериментов. `check-code.bat` прошёл успешно. Результаты предыдущего эксперимента с `@BeforeSuite` относятся к коммиту `5fc9ef5`, отменённому коммитом `445b9bf`.