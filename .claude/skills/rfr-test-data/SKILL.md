---
name: rfr-test-data
description: Create and maintain test users and preconditions in Rangiffler rfr-e2e.
---

# rfr-test-data v1

- Give every test user unique generated credentials; use `TestUser.unique()` instead of fixed identities.
- Let the `UserFixture` decorator tree own users, profiles, and relationships it creates.
- Register the root fixture before creation so Store cleanup can handle partial setup failures.
- Model each new user precondition as a `UserFixture` decorator and compose it in `UserRequest.fixture()`.
- Keep `UserExtension` generic; do not add precondition branches or ownership state there.
- Keep SQL and persistence operations in row objects under `data/db`; keep SQL out of `jupiter`, fixtures, and tests.
- Create `ApiProfile` explicitly whenever a test user needs an API profile; an auth account does not imply one.
- Close resources in reverse creation order through `TearDown`; remove owned relationships before related users and delegates.
- Use a pool of pre-seeded users only when the required data cannot be created through supported test-data objects; document who consumes and cleans them up.
