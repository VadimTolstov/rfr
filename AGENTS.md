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

> 💡 **Модульные инструкции:**
> - `rfr-auth/AGENTS.md` — OAuth Server, токены, CSRF
> - `rfr-api/AGENTS.md` — GraphQL API, что mock, а что реально
> - `rfr-e2e/AGENTS.md` — E2E-тесты, Page Objects, фикстуры
> - `rfr-gql-client/AGENTS.md` — React SPA, Apollo, MUI
>
> При работе в модуле **обязательно** читай соответствующий файл.

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

# запустить auth + api параллельно
./gradlew runAll        
            
# прогнать все e2e-тесты (всегда запускаются, даже без изменений в коде)
./gradlew e2e                 
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

## 4. Формат веток

Наблюдение по git-истории:

| Префикс | Назначение | Пример |
|---------|------------|--------|
| `hw/` | Домашние задания | `hw/01-profile-tests` |
| `vN/` | Версии уроков | `v3/l01-first-e2e` |
| `feature/` | Новые фичи (фронтенд) | `feat/user-friends` |
| `master` | Основная ветка | `master` |

Имя ветки: `hw/N-имя` для домашних заданий, `feature/описание` для фич.

---

## 5. Формат коммитов

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

## 6. Зависимости и версии

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

## 7. Локальная настройка

### Подъём MySQL (безопасно)

```bash
# удаляет только rfr-mysql, данные в volume сохраняются
bash localenv.sh   
```

Или вручную, если контейнер уже есть:
docker start rfr-mysql

### Запуск сервисов (в порядке важности)

```bash
./gradlew runAll   # поднимает rfr-auth и rfr-api параллельно
cd rfr-gql-client && npm run dev   # фронт на :3001
```

## 8. Запрещено

- **Не запускать `ports.sh`** — он убивает процессы на портах 3001/8081/9001/3306, в том числе уже работающий стенд.
- **Никогда не использовать `rm` для удаления файлов.** Если нужно удалить файл, переименовать его в `.<имя>.<timestamp>.bak` (например, `.file.txt.20231102.bak`) и предложить пользователю удалить резервную копию позже.
- **Не делать `git commit` / `git push` без явной просьбы.**
- **Не трогать `application.yml`, `build.gradle`, `docker-compose.yml` без явной просьбы.**
- **Не менять файлы вне `rfr-e2e/` при работе над тестами** (auth/api/frontend — чужая зона).
- **При сомнениях — спрашивать, не догадываться.**

---

## 9. Язык общения

- **Общайся со мной исключительно на русском языке.**
- Код, названия переменных и классов, пути к файлам, команды терминала, логи и сообщения коммитов оставляй на английском (как принято в проекте).

---

## 10. Использование RTK (экономия токенов)
Все команды терминала выполняй с префиксом `rtk`. Если в песочнице возникает ошибка «rtk не распознается», используй полный путь к исполняемому файлу:
`"C:\Users\Vadim\AppData\Local\Microsoft\WinGet\Links\rtk.exe" git status`

## 11. Автоматическая проверка (Feedback Loop)
После любых правок в коде (Java, TypeScript) ты обязан запустить `check-code.bat`. Скрипт сам определит, какой модуль скомпилировать.

Правила цикла:
- Если скрипт упал — прочитай ошибку, исправь код, запусти снова.
- **Максимум 3 итерации.** Если после 3 попыток ошибка сохраняется — остановись и напиши мне: "STUCK: ошибка не исправляется автоматически, нужна помощь. Ошибка: <текст>".
- **Если одна и та же ошибка повторяется 2 раза подряд** — не пытайся исправить её тем же способом. Напиши мне: "LOOP DETECTED: <текст ошибки>" и остановись.
- **Запрещено** «обходить» ошибку: комментировать код, ставить `// TODO`, удалять методы, менять `build.gradle`.
