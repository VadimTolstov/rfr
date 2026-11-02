# AGENTS.md — Rangiffler

> Контекст для AI-агентов и новых участников проекта.

---

## 1. Структура проекта

Проект — **мультипроект Gradle** (4 модуля) + фронтенд-пакет внутри одного из них.

```
rfr/
├── rfr-api/          # Spring Boot GraphQL API (port 8081)
├── rfr-auth/         # Spring Boot OAuth2 Authorization Server (port 9001)
├── rfr-e2e/          # E2E-тесты (JUnit 6 + Playwright Java)
└── rfr-gql-client/   # React/TypeScript SPA (Vite + MUI, port 3001)
```

### rfr-api (GraphQL API)
- **Пакет:** `io.student.rangiffler`
- **Технологии:** Spring Boot 4.1.1, DGS codegen, Spring GraphQL, Spring Data JPA, Flyway, Lombok, MySQL 8
- **Структура:**
  - `config/` — конфигурации (GraphQlExceptionResolver, CorsCustomizer)
  - `controller/` — GraphQL-контроллеры (User, Country, Feed, Photo)
  - `data/entity/` — JPA-сущности (User, Country, Friendship)
  - `data/repository/` — Spring Data репозитории
  - `service/api/` — бизнес-логика (UserService, CountryService)
  - `service/mock/` — мок-данные для Feed
  - `exception/` — пользовательские исключения
  - `util/` — утилиты (BytesAsString, GqlQueryPaginationAndSort)
- **Schema:** `src/main/resources/graphql/query.graphqls`
- **DB migration:** `src/main/resources/db/migration/rangiffler-api/`
- **База данных:** MySQL `rangiffler-api` (localhost:3306)

### rfr-auth (OAuth2 Authorization Server)
- **Пакет:** `io.student.rangiffler`
- **Технологии:** Spring Boot 4.1.1, OAuth2 Authorization Server, Thymeleaf, Spring Data JPA, Flyway, Lombok, MySQL 8
- **Структура:**
  - `config/` — Security, AccessTokenCustomizer, ThemeCookieFilter
  - `controller/` — Login, Register, CustomError
  - `data/` — UserEntity, AuthorityEntity, UserRepository
  - `model/` — RegistrationForm
  - `service/` — UserService, DatabaseUserDetailsService, GlobalExceptionHandler
  - `validation/` — кастомные валидаторы (EqualPasswords, NoWhitespace)
- **Темплейты:** `src/main/resources/templates/{login,register,error}.html`
- **Статика:** шрифты, изображения, CSS
- **DB migration:** `src/main/resources/db/migration/rangiffler-auth/`
- **База данных:** MySQL `rangiffler-auth` (localhost:3306)

### rfr-e2e (E2E-тесты)
- **Пакет:** `io.student.rangiffler`
- **Технологии:** JUnit Jupiter 6.1.3, Playwright Java 1.63.0, Allure 3, AspectJ
- **Структура:**
  - `test/` — тесты
  - `test/fake/` — заглушки (SimpleTest)
  - `test/java/io/student/rangiffler/ui/page/` — Page Objects (создавать здесь)
  - `test/java/io/student/rangiffler/ui/component/` — UI компоненты (создавать здесь)
  - `test/java/io/student/rangiffler/config/` — конфигурации (создавать здесь)
  - `test/java/io/student/rangiffler/data/` — модели (`TestUser`) и фикстуры (`UserFixture`)
  - `test/java/io/student/rangiffler/data/db/` — работа с БД (`Databases`, `Jdbc`, `JdbcClient`)
  - `test/java/io/student/rangiffler/jupiter/extension/` — JUnit 5 extensions (`UserExtension`)
  - `test/java/io/student/rangiffler/jupiter/annotation/` — кастомные аннотации (`@User`)
- **Конфигурация параллелизма:** `src/test/resources/junit-platform.properties`
  - Параллельное выполнение классов и методов, параллелизм = 3

### rfr-gql-client (Frontend)
- **Технологии:** React 19, TypeScript 6, Vite 8, MUI 9, Apollo Client 4, React Router 8
- **Структура:**
  - `src/api/` — GraphQL-клиент (Apollo)
  - `src/pages/` — страницы приложения
  - `src/components/` — переиспользуемые компоненты
  - `src/context/` — React Context
  - `src/hooks/` — кастомные хуки
  - `src/types/` — TypeScript-типы
  - `src/utils/` — утилиты
  - `src/assets/` — статика
- **ESLint:** flat config, strict mode, no unused locals/params

---

## 2. Порты сервисов

| Сервис | Порт |
|--------|------|
| Frontend (Vite) | **3001** |
| rfr-api (GraphQL) | **8081** |
| rfr-auth (OAuth2) | **9001** |
| MySQL | **3306** |

---

## 3. Команды сборки, запуска, тестов

### Gradle (Java-модули: rfr-api, rfr-auth, rfr-e2e)

```bash
# Сборка всего проекта
./gradlew build

# Сборка конкретного модуля
./gradlew :rfr-api:build
./gradlew :rfr-auth:build

# Запуск E2E-тестов (один тест)
./gradlew :rfr-e2e:test --tests 'io.student.rangiffler.test.fake.SimpleTest' --rerun-tasks

# Запуск E2E-тестов (все)
./gradlew :rfr-e2e:test

# Запуск конкретного теста (PowerShell)
./gradlew :rfr-e2e:test --tests 'SimpleTest' --rerun-tasks
```

### npm (rfr-gql-client)

```bash
cd rfr-gql-client

# Установка зависимостей
npm install

# Режим разработки (port 3001)
npm run dev

# Сборка продакшн-бандла
npm run build

# Линтинг
npm run lint

# Превью собранного бандла
npm run preview
```

### Docker (MySQL)

```bash
# Поднять MySQL-контейнер (если ещё не поднят)
docker run --name rfr-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=secret -d mysql:8.4.7

# Если контейнер уже существует
docker start rfr-mysql
```

---

## 4. Кодстайл Page Objects (rfr-e2e)

> Это краткая выжимка. Полный контракт — в skill `.claude/skills/rfr-page-objects/SKILL.md`.
> При расхождении приоритет у skill'а.

### Структура пакетов
- **Page Objects:** `io.student.rangiffler.ui.page`
- **UI компоненты:** `io.student.rangiffler.ui.component`
- **Конфигурации:** `io.student.rangiffler.config`

### Конструктор
- Класс `final`, поля `private final`: `Page`, `Locator`, компоненты
- Конструктор принимает **только `Page`** и **только присваивает поля**
- **Без** переходов, кликов, проверок, ожиданий, SQL

### Методы
- Каждый публичный метод — с `@Step`, каждый параметр в тексте: `@Step("Заполнить форму входа: {username} / {password}")`
- Переход → возвращает объект следующей страницы; действие/проверка → `this`
- Одно бизнес-действие — один метод, имя по форме: `fillLoginForm(...)`, `fillRegistrationForm(...)`
- Без безликих `fillForm`/`fill`/`submit`

### Проверки
- `fill…` / `click…` — **ничего не проверяют**
- `checkThat…` — **только проверяют** через `PlaywrightAssertions.assertThat(locator)`
- Без `checkThatOpened`, `checkPageLoaded`, `isLoaded`, `waitUntilLoaded`
- Без `locator.isVisible()` в JUnit-проверках и ручных ожиданий
- Ожидаемое значение приходит из теста: `checkThatUsername(username)`

### Локаторы
- `getByRole` с точным `name`, `getByLabel`, `getByPlaceholder`, `getByText`, `getByTestId`
- Без `first()`/`nth()`, MUI-классов, XPath
- Неуникальный элемент сужать родителем: `page.getByRole(BANNER).getByRole(...)`

### Общее
- Композиция вместо наследования (общие части — компоненты)
- Вход только через UI, `access_token` в `localStorage` не подставлять
- Без комментариев и Javadoc

---

## 5. Кодстайл тестов (rfr-e2e)

- JUnit Jupiter 6.x (`org.junit.jupiter.api.Test`, `@Test`)
- Allure интеграция: `Allure.step("Название шага", () -> { ... })`
- Playwright assertions: `PlaywrightAssertions.assertThat(locator)`
- Параллельное выполнение включено (parallelism = 3)
- Фикстуры и SQL — вне ответственности Page Objects

### Подготовка данных и фикстуры
- Данные тестовых пользователей подготавливай через БД с помощью Spring `JdbcClient` и `CloseableResource`, чтобы гарантировать очистку.
- Не хардкодь пользовательские данные в тестах.
- Используй `@ExtendWith(PageFixture.class)` для управления браузером.
- Используй `@RegisterExtension static UserExtension USER_EXTENSION = new UserExtension();` для создания пользователей в БД и указывай `@User TestUser user` в параметрах теста.

---

## 6. Формат веток

Наблюдение по git-истории:

| Префикс | Назначение | Пример |
|---------|------------|--------|
| `hw/` | Домашние задания | `hw/01-profile-tests` |
| `vN/` | Версии уроков | `v3/l01-first-e2e` |
| `feature/` | Новые фичи (фронтенд) | `feat/user-friends` |
| `master` | Основная ветка | `master` |

Имя ветки: `hw/N-имя` для домашних заданий, `feature/описание` для фич.

---

## 7. Формат коммитов

История показывает два стиля:

### С префиксом (Conventional Commits)
```
type: описание
```

Типы:
- `feat:` — новая функциональность
- `fix:` — исправление бага
- `build:` — изменения сборки/зависимостей
- `docs:` — документация
- `change:` — изменение языка/стиля

Примеры:
```
feat: redesign UI, add country filter and single-row friendship model
build: upgrade frontend dependencies to latest versions
docs: actualize README for current backend, frontend and e2e stack
fix: resolve audit defects in friendship, GraphQL contracts and frontend
```

### Без префикса
```
Описание действия
```

Примеры:
```
Use access_token instead id_token. Update README
Resize map component
Change language
```

### Merge-коммиты
```
Merge pull request #N from branch-name
```

### Рекомендуемый формат
```
type: краткое описание на английском (Imperative mood)

Детали (опционально)
```

---

## 8. Зависимости и версии

| Компонент | Версия |
|-----------|--------|
| Java | 25 |
| Gradle | 9.7.1 |
| Spring Boot | 4.1.1 |
| JUnit Jupiter | 6.1.3 |
| Playwright | 1.63.0 |
| Allure | 3.0.0 (Java), 3.17.0 (report) |
| AspectJ | 1.9.25.1 |
| Lombok | 1.18.48 |
| MySQL | 8.0.33 |
| Flyway | 13.7.0 |
| React | 19.3.0 |
| TypeScript | ~6.0.3 |
| Vite | 8.3.0 |
| MUI | 9.4.0 |
| Apollo Client | 4.3.0 |

---

## 9. Локальная настройка

### Подъём MySQL (безопасно, без localenv.sh)
docker run --name rfr-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=secret -d mysql:8.4.7

Если контейнер уже существует:
docker start rfr-mysql

### Установка браузеров Playwright (один раз)
./gradlew :rfr-e2e:installPlaywright

### Запуск сервисов (в порядке важности)
1. rfr-auth (первым!): ./gradlew :rfr-auth:bootRun    # порт 9001
2. rfr-api:               ./gradlew :rfr-api:bootRun    # порт 8081
3. rfr-gql-client:        cd rfr-gql-client && npm run dev  # порт 3001

### Прогон E2E-тестов
./gradlew :rfr-e2e:test --tests '*RegistrationTest' --tests '*MainPageTest' --rerun-tasks
./gradlew :rfr-e2e:allureReport


## 10. Запрещено

- **Не запускать `localenv.sh`** — он останавливает и удаляет ВСЕ Docker-контейнеры на машине.
- **Не запускать `ports.sh`** — он убивает процессы на портах 3001/8081/9001/3306, в том числе уже работающий стенд.
- **Не делать `git commit` / `git push` без явной просьбы.**
- **Не трогать `application.yml`, `build.gradle`, `docker-compose.yml` без явной просьбы.**
- **Не менять файлы вне `rfr-e2e/` при работе над тестами** (auth/api/frontend — чужая зона).
- **При сомнениях — спрашивать, не догадываться.**

---

## 11. Язык общения

- **Общайся со мной исключительно на русском языке.**
- Код, названия переменных и классов, пути к файлам, команды терминала, логи и сообщения коммитов оставляй на английском (как принято в проекте).

---

## 12. Использование RTK (экономия токенов)
Все команды терминала выполняй с префиксом `rtk`. Если в песочнице возникает ошибка «rtk не распознается», используй полный путь к исполняемому файлу:
`"C:\Users\Vadim\AppData\Local\Microsoft\WinGet\Links\rtk.exe" git status`