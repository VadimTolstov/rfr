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

Задача `e2e` запускает только UI-тесты и включает `rfr.standCheck` и `rfr.migrationCheck`. `StandReadinessExtension` ждёт доступности MySQL, auth, API и фронтенда, затем `StandSchemaExtension` проверяет миграции. Ожидание выполняется внутри Jupiter через `SuiteExtension`; зависимости задачи `e2e` от `waitForStand` больше нет. Расширение не запускает и не перезапускает сервисы.

Ручная задача `:rfr-e2e:waitForStand` сохранена для запуска `scripts/wait-for-stand.sh`, принимает `WAIT_TIMEOUT` и требует Bash. Только эта задача выбирает Bash из `RFR_BASH`, `rfr.bash`, стандартного каталога Git Bash или `PATH`; Java-ожидание внутри тестов от Bash не зависит.

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
| `rfr.environmentDb` | `RFR_ENVIRONMENT_DB` | `false` (`e2e` включает `true`) |
| `rfr.standCheck` | `RFR_STAND_CHECK` | `false` |
| `rfr.standTimeout` | `RFR_STAND_TIMEOUT` | `180` секунд |
| `rfr.authMigrations` | `RFR_AUTH_MIGRATIONS` | `../rfr-auth/src/main/resources/db/migration/rangiffler-auth` |
| `rfr.apiMigrations` | `RFR_API_MIGRATIONS` | `../rfr-api/src/main/resources/db/migration/rangiffler-api` |
| `rfr.browser` | `RFR_BROWSER` | `chromium` |
| `rfr.headless` | `RFR_HEADLESS` | `true` |
| `rfr.commit` | `RFR_COMMIT` | `unavailable` при запуске вне Gradle |

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

## Ожидание стенда

`StandReadinessExtension` использует адреса из `TestConfig` и те же признаки готовности, что и `scripts/wait-for-stand.sh`:

| Сервис | Признак готовности | Команда запуска в ошибке |
|---|---|---|
| MySQL | TCP-соединение к хосту и порту `rfr.dbUrl`, порт по умолчанию 3306 | `docker compose up -d --wait` |
| rfr-auth | GET `rfr.authUrl/.well-known/openid-configuration`, HTTP-код меньше 400 | `./gradlew runAll` |
| rfr-api | POST `rfr.apiUrl/graphql`, `Content-Type: application/json`, тело `{"query":"{__typename}"}`, любой HTTP-ответ | `./gradlew runAll` |
| rfr-gql-client | GET `rfr.frontUrl`, HTTP-код меньше 400 | `cd rfr-gql-client && npm run dev` |

API намеренно принимает и HTTP-ошибку: исходная команда `curl` для GraphQL не использует `-f`, в отличие от auth и фронтенда. HTTP-пробы используют HTTP/1.1: автоматическое предложение `Upgrade: h2c` Java-клиентом направляет запрос Vite в обработчик смены протокола, где обычная проверка страницы не получает ответа.

Общий срок считается по монотонным часам. Попытка ограничена меньшим из оставшегося времени и пяти секунд; отдельно ограничены подключение и HTTP-запрос. Внешний лимит `Future.get` ограничивает также DNS и чтение тела ответа. Между попытками — не больше 200 мс. Сервисы, которые уже ответили, повторно не опрашиваются, как в скрипте. Значение `standTimeout` должно быть положительным целым числом секунд. Прерывание восстанавливает флаг потока и прекращает ожидание; отмена виртуальных потоков не задерживает выход за пределы общего срока.

При истечении срока исключение перечисляет оставшиеся сервисы и команды их запуска. `SuiteRun` сохраняет ошибку в корневом Store; остальные тестовые классы получают ту же ошибку без нового ожидания. Миграции и тела тестов после ошибки готовности не выполняются.

### Порядок расширений и обоснование для PR

Оба расширения зарегистрированы в одном `META-INF/services/org.junit.jupiter.api.extension.Extension`: сначала `StandReadinessExtension`, затем `StandSchemaExtension`. На используемом test classpath `ServiceLoader` обходит имена провайдеров внутри одного файла в порядке строк, Jupiter сохраняет порядок регистрации для before-callbacks. Поэтому каждый класс завершает readiness-callback до вызова schema-callback; синхронизированный `SuiteRun.start()` заставляет параллельные классы дождаться того же результата проверки. При ошибке readiness следующие before-callbacks не вызываются. Гарантия относится к двум провайдерам этого файла, а не к произвольному порядку разных JAR или JPMS-модулей. Порядок дополнительно проверяется тестом через настоящий `ServiceLoader`.

Источники: [ServiceLoader — порядок провайдеров](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/ServiceLoader.html), [автоматическая регистрация расширений Jupiter](https://docs.junit.org/6.1.0/extensions/registering-extensions).

### Запуск из IDE

В JUnit-конфигурации UI-теста задать VM options `-Drfr.standCheck=true`; для короткой проверки добавить `-Drfr.standTimeout=2`. По согласованному правилу значение по умолчанию — `false`: прямой UI-запуск без свойства ожидание не включает. При необходимости сверки миграций отдельно включить `-Drfr.migrationCheck=true` и корректно задать пути миграций. `:rfr-e2e:test` свойство `standCheck` не включает.

Через MCP `idea` проверен запуск `MainPageTest` с `standCheck=true`, сроком 2 секунды и адресом фронтенда на свободном loopback-порту без сервиса. Работающий фронтенд не останавливался. IDE показывает ошибку `Class Configuration` с текстом `Stand did not become ready within 2 seconds; unavailable services: rfr-gql-client (start with 'cd rfr-gql-client && npm run dev')`, код завершения `-1`; UI-тест отмечен пропущенным, его тело и Playwright-навигация не выполняются. Лог сохранён в `build/verification/readiness/idea-unavailable-front.log`.

### Проверки расширения

`StandReadinessExtensionTest` запускает мишень `@ProbeTarget` через `EngineTestKit`. Каждый тест создаёт свой `HttpServer` на эфемерном порту и TCP-слушатель вместо MySQL; системные свойства JVM не меняются. Адреса, флаг и срок передаются параметрами вложенного запуска Jupiter; вне таких запусков используются значения `TestConfig`.

Проверены: фронтенд начинает отвечать через 2 секунды и тест выполняется; фронтенд не отвечает до срока и тест не запускается, а ошибка содержит его имя и команду; выключенная проверка не отправляет HTTP-запросов; зависшее тело HTTP-ответа не продлевает общий срок; ServiceLoader ставит ожидание раньше сверки миграций. Подставной сервер отклоняет протокольный Upgrade, чтобы проверять совместимость с обычной HTTP readiness-точкой.

```bash
./gradlew :rfr-e2e:test --tests '*StandReadinessExtensionTest'
./gradlew :rfr-e2e:test
./gradlew e2e
```

Проверено 2026-11-11: все пять проверок `StandReadinessExtensionTest`, обычная задача `test` и 16 UI-тестов `e2e` прошли. `check-code.bat` и проверки новых классов в IDEA завершились без ошибок и предупреждений. Итоговый лог Gradle: `build/verification/readiness/final.log`; задачи `waitForStand` в нём нет.

## Окружение в Allure

`AllureEnvironmentExtension` реализует `SuiteExtension` и пишет `environment.properties` в `afterSuite`. Каталог задаётся системным свойством `allure.results.directory`; без него используется `rfr-e2e/build/allure-results` относительно рабочей директории. Gradle-плагин Allure передаёт абсолютный каталог результатов. Для собственного запуска из IDE при необходимости задайте абсолютный путь явно.

| Ключ | Источник |
|---|---|
| `frontUrl`, `authUrl`, `apiUrl` | `TestConfig` |
| `browser`, `headless` | `TestConfig` |
| `commit` | Короткий хеш HEAD, полученный Gradle через `git rev-parse --short HEAD` и переданный как `rfr.commit` |
| `migration.rangiffler-auth` | Текущая применённая версия auth-схемы из `Flyway.info().current()` |
| `migration.rangiffler-api` | Текущая применённая версия API-схемы из `Flyway.info().current()` |

Тестовый Java-код не запускает Git. При переданном вручную `-Drfr.commit=...` Gradle сохраняет это значение. Чтение версий включается только при `rfr.environmentDb=true`; обычный `test` его не включает, `e2e` включает. При выключенном флаге Allure не открывает соединений к MySQL, но сохраняет все восемь ключей и пишет `unavailable` для обеих схем. При включённом флаге версии читаются отдельными подключениями с JDBC `connectTimeout=1000` и `socketTimeout=1000`, без повторных подключений Flyway и без Hikari-пулов фикстур. Если схему прочитать нельзя, записывается `unavailable`; если история не содержит текущей версионной миграции — `none`. Учётные данные в отчёт не попадают. Ошибка записи самого файла выбрасывается, а не скрывается.

### Почему расширение зарегистрировано первым

Порядок в одном service-файле: **AllureEnvironmentExtension → StandReadinessExtension → StandSchemaExtension**. Эксперимент `AllureEnvironmentExtensionTest` подтверждает: когда `beforeSuite` другой реализации падает, Jupiter прекращает цепочку `beforeAll`, следующий callback не вызывается и тела тестов не выполняются. Поэтому последняя регистрация окружения не гарантировала бы вызов его `afterSuite`.

Первая регистрация успевает поместить свой `SuiteRun` в корневой `ExtensionContext.Store` до проверки готовности и миграций. JUnit закрывает Store и вызывает `SuiteRun.close()` даже после ошибки подготовки; тот вызывает `AllureEnvironmentExtension.afterSuite`. Это гарантирует запись и после ошибки другого `beforeSuite`, пока процесс JVM штатно завершает работу JUnit. При аварийном завершении JVM callbacks не гарантируются.

Проверки `EngineTestKit` через мишени `@ProbeTarget`: файл после успешного прогона содержит все восемь ключей; после упавшего `beforeSuite` файл тоже существует, а следующий before-callback не вызван; недоступная MySQL при включённом чтении даёт две версии `unavailable` и не мешает записи. Два дополнительных теста с TCP-счётчиком проверяют ноль подключений по умолчанию и реальные попытки подключения при `environmentDb=true`. В тестах готовности стенда автозагрузка этого расширения исключена для изоляции: получение метаданных не должно добавлять сетевые запросы к измерению таймаута readiness.

```bash
./gradlew :rfr-e2e:test --tests '*AllureEnvironmentExtensionTest'
./gradlew allureServe
```

Перепроверка 2026-11-12 на `aca8baf` подтвердила два успешных `e2e`: по 16 UI-тестов без ошибок и пропусков. Прежняя недоступность фронтенда воспроизвелась как различие IPv4/IPv6: Java не подключалась к `localhost:3001`, хотя curl получал ответ через `[::1]`. После запуска Vite на `127.0.0.1:3001` первый e2e дождался сервиса и прошёл за 2 мин 41 с, второй прошёл за 22 с. Подробности и отдельные XML обоих запусков — в [отчёте ДЗ 4](../docs/hw4/README.md).
