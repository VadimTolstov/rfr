package io.student.rangiffler.test.people;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.student.rangiffler.data.Relation;
import io.student.rangiffler.data.SingleUserFixture;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.data.UserFixture;
import io.student.rangiffler.data.UserWithRelationsFixture;
import io.student.rangiffler.data.db.ApiProfile;
import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.FriendshipStatus;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

/** Verifies ownership and partial cleanup against the AUTH and API schemas. */
final class UserFixtureOwnershipTest {
  @Test
  @DisplayName("Фикстура при конфликте AUTH сохраняет данные действующего владельца")
  void preservesOwnerWhileAnotherFixtureFails() {
    TestUser user = TestUser.unique();
    try (UserFixture owner = new SingleUserFixture(user)) {
      owner.create();
      try (UserFixture fixture = new SingleUserFixture(user)) {
        assertThrows(DuplicateKeyException.class, fixture::create);
      }
      assertAll(
          () -> assertTrue(account(user).exists()),
          () -> assertTrue(profile(user).exists())
      );
    }
    assertAbsent(user);
  }

  @Test
  @DisplayName("Конфликт API удаляет созданный AUTH и сохраняет чужой профиль")
  void releasesOwnAccountButPreservesExistingProfile() {
    TestUser user = TestUser.unique();
    ApiProfile profile = profile(user);
    UUID id = UUID.randomUUID();
    profile.create(id);
    try {
      try (UserFixture fixture = new SingleUserFixture(user)) {
        assertThrows(DuplicateKeyException.class, fixture::create);
      }
      assertAll(
          () -> assertFalse(account(user).exists()),
          () -> assertTrue(profile.exists())
      );
    } finally {
      profile.delete(id);
    }
  }

  @Test
  @DisplayName("Частично созданные связи освобождаются без удаления конфликтующего пользователя")
  void releasesOwnRelatedResourcesAfterPartialCreation() {
    TestUser origin = TestUser.unique();
    TestUser friend = TestUser.unique();
    TestUser existing = TestUser.unique();
    TestUser skipped = TestUser.unique();
    TestUser user = new TestUser(origin.username(), origin.password(),
        List.of(friend, existing, skipped), List.of(), List.of());
    Friendship friendship = new Friendship(user.username(), friend.username(), FriendshipStatus.ACCEPTED);
    try (UserFixture owner = new SingleUserFixture(existing)) {
      owner.create();
      try (UserFixture fixture = new UserWithRelationsFixture(new SingleUserFixture(user), Relation.FRIEND)) {
        assertThrows(DuplicateKeyException.class, fixture::create);
        assertTrue(friendship.exists());
      }
      assertAll(
          () -> assertFalse(friendship.exists()),
          () -> assertAbsent(user),
          () -> assertAbsent(friend),
          () -> assertAbsent(skipped),
          () -> assertTrue(account(existing).exists()),
          () -> assertTrue(profile(existing).exists())
      );
    }
  }

  @Test
  @DisplayName("Закрытие прежней фикстуры сохраняет новые записи с тем же именем")
  void preservesReplacementRowsWithSameUsername() {
    TestUser user = TestUser.unique();
    try (UserFixture replacement = new SingleUserFixture(user)) {
      try (UserFixture previous = new SingleUserFixture(user)) {
        previous.create();
        profile(user).delete();
        account(user).delete();
        replacement.create();
      }
      assertAll(
          () -> assertTrue(account(user).exists()),
          () -> assertTrue(profile(user).exists())
      );
    }
    assertAbsent(user);
  }

  private AuthAccount account(TestUser user) {
    return new AuthAccount(user.username(), user.password());
  }

  private ApiProfile profile(TestUser user) {
    return new ApiProfile(user.username());
  }

  private void assertAbsent(TestUser user) {
    assertAll(
        () -> assertFalse(account(user).exists()),
        () -> assertFalse(profile(user).exists())
    );
  }
}
