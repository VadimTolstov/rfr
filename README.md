# Rangiffler

> **Приватный репозиторий.** Здесь доступен исходный код из видеоуроков курса
> **QA.GURU Java Advanced (Playwright)**.
>
> Исходно репозиторий является копией [qa-guru/rfr](https://github.com/qa-guru/rfr).


# Текущая структура проекта

```
rfr/
├── rfr-auth/              # OAuth 2.0 Authorization Server (готов)
├── rfr-api/               # GraphQL API Backend (базовая реализация)
│   ├── Mock контроллеры для фотографий
│   ├── Реальные контроллеры для пользователей и стран
│   └── Flyway миграции БД
├── rfr-gql-client/        # React + TypeScript Frontend (готов)
└── rfr-e2e/               # E2E тесты: JUnit 6, Playwright Java, Allure 3
```

**Порты:**
- rfr-auth: 9001
- rfr-api: 8081
- rfr-gql-client: 3001

**Технологии:**
- Сборка: Java 25, Gradle 9.7.1
- Backend: Spring Boot 4.1.1, Spring for GraphQL, Spring Security, JPA, Flyway
- Frontend: React 19, TypeScript 6, Vite 8, Apollo Client 4, Material UI 9, React Router 8
- Auth: Spring Authorization Server
- БД: MySQL 8.4
- E2E: JUnit 6, Playwright Java 1.63, Allure 3


#### 1. Запусти зависимости (БД):

```bash
bash localenv.sh
```

Скрипт запустит MySQL в Docker контейнере.

#### 2. Запусти rfr-auth:

```bash
cd rfr-auth
../gradlew bootRun
```

Или запусти класс с методом main руками. Auth будет доступен на порту 9001: http://localhost:9001

#### 3. Запусти rfr-api:

```bash
cd rfr-api
../gradlew bootRun
```

rfr-api стартанет на порту 8081: http://localhost:8081/graphql

GraphiQL интерфейс доступен по адресу: http://localhost:8081/graphiql

#### 4. Обнови зависимости и запускай фронт:

```bash
cd rfr-gql-client
npm i
npm run dev
```

Фронт стартанет в твоем браузере на порту 3001: http://localhost:3001/

#### 5. Проверь работоспособность

Кнопка "Login" работает через сервис auth. После успешной авторизации ты попадешь на главную страницу Rangiffler.

Mock контроллеры в rfr-api будут возвращать статические данные из JSON файлов для ленты фотографий.
Реальная работа с пользователями и странами уже реализована через базу данных.