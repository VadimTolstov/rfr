---
name: unit-tests
description: Правила написания юнит-тестов (TDD, один assert, изолированность).
---

# Unit Tests

- TDD: сначала красный тест, потом код, потом зелёный.
- Один тест — одно поведение, один assert последним выражением.
- Никаких `setUp`/`tearDown` с shared state. Каждый тест изолирован.
- Prefer fakes over mocks. Никогда не мокай файловую систему и память.
- Имена тестов — как предложения: `shouldNotAddDuplicateUser()`.
- Никаких `@SpringBootTest` — только `@ExtendWith(MockitoExtension.class)`.
- Не тестируй геттеры, конструкторы, side effects.