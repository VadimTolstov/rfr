package io.student.rangiffler.data;

import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.data.db.ApiProfile;
import java.util.UUID;

/** Owns the AUTH account and API profile acquired for one test user. */
public final class SingleUserFixture implements UserFixture {
  private final TestUser user;
  private final ApiProfile profile;
  private final AuthAccount account;
  private final UUID id = UUID.randomUUID();
  private boolean ownsAccount;
  private boolean ownsProfile;

  public SingleUserFixture(TestUser user) {
    this.user = user;
    profile = new ApiProfile(user.username());
    account = new AuthAccount(user.username(), user.password());
  }

  @Override
  public void create() {
    if (ownsAccount || ownsProfile) {
      throw new IllegalStateException("Close the user fixture before creating it again");
    }
    account.create(id);
    ownsAccount = true;
    try {
      profile.create(id);
      ownsProfile = true;
    } catch (RuntimeException | Error exception) {
      try {
        closeAccount();
      } catch (RuntimeException | Error cleanupException) {
        exception.addSuppressed(cleanupException);
      }
      throw exception;
    }
  }

  @Override
  public TestUser user() {
    return user;
  }

  @Override
  public void close() {
    new TearDown()
        .with(this::closeProfile)
        .with(this::closeAccount)
        .close();
  }

  private void closeProfile() {
    if (ownsProfile) {
      profile.delete(id);
      ownsProfile = false;
    }
  }

  private void closeAccount() {
    if (ownsAccount) {
      account.delete(id);
      ownsAccount = false;
    }
  }
}
