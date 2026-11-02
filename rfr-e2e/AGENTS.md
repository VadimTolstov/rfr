# AGENTS.md — Модуль rfr-e2e (E2E-тесты)

> Локальные инструкции для AI-агента. Применяются при работе внутри модуля `rfr-e2e`.
> Детальная архитектура пакетов и конфигурации — `rfr-e2e/README.md`.

## 🧠 Обязательный Skill
При написании или изменении любых Page Objects и UI-компонентов **обязательно** прочитай и следуй контракту из skill:
`.claude/skills/rfr-page-objects/SKILL.md`

## Конфигурация
- Все URL и настройки БД берутся **строго** из `TestConfig`.
- Ключи для `System.getProperty` имеют префикс `rfr.` (например, `rfr.frontUrl`).
- Переменные окружения (ENV) пишутся в `UPPER_SNAKE_CASE` с префиксом `RFR_` (например, `RFR_FRONT_URL`).

## Архитектура и пакеты
- **Page Objects:** `io.student.rangiffler.ui.page`
- **UI компоненты:** `io.student.rangiffler.ui.component`
- **Тесты:** `io.student.rangiffler.test`
- **Фикстуры и Extensions:** `io.student.rangiffler.jupiter`, `io.student.rangiffler.data`
- Базовых классов нет (только композиция).

## Правила написания Page Objects
- Класс `final`, поля `private final`. Конструктор принимает **только `Page`** и только присваивает поля.
- Каждый публичный метод — с `@Step`.
- Действие отдельно от проверки: `fill…`/`click…` ничего не проверяют; `checkThat…` только проверяют через `PlaywrightAssertions.assertThat(locator)`.
- **Локаторы:** `getByRole` с точным `name`, `getByLabel`, `getByPlaceholder`. Без `first()`/`nth()`. Неуникальный элемент сужай родителем.
- Для MUI `Select` открывай `COMBOBOX` и выбирай `OPTION`, отфильтрованный по `IMG` с точным именем (страна France — «France France»).
- При мутациях (например, `clickSave`) используй `page.waitForResponse`, чтобы `reload()` не обогнал мутацию.

## Правила написания тестов
- Используй JUnit 5: `@ExtendWith(PageFixture.class)` и `@RegisterExtension static UserExtension`.
- **Данные:** Создавай пользователей только через `@User TestUser user` (БД, `JdbcClient`, `CloseableResource`).
- **Fluent-интерфейс:** Тест должен быть одной цепочкой вызовов. Никаких `Allure.step` вокруг всего теста.
- **Аннотации:** Используй `@DisplayName`.

## Запуск и проверка
- Проход сценария через MCP — это **не** запуск теста.
- Успех подтверждается только прогоном Gradle:
    - Запуск всех тестов: `./gradlew :rfr-e2e:test`
    - Запуск конкретного теста: `./gradlew :rfr-e2e:test --tests '<Тест>' --rerun-tasks`
- Установка браузеров (один раз): `./gradlew :rfr-e2e:installPlaywright`

## Запрещено
- **Никогда не хардкодь URL, пароли или тестовые данные в тестах и Page Objects.** Всё должно браться из `TestConfig` или фикстур.