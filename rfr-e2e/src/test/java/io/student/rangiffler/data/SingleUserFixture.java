package io.student.rangiffler.data;

import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.data.db.ApiProfile;

public final class SingleUserFixture implements UserFixture {
  private final TestUser user;
  private final ApiProfile profile;
  private final AuthAccount account;

  public SingleUserFixture(TestUser user) {
    this.user = user;
    profile = new ApiProfile(user.username());
    account = new AuthAccount(user.username(), user.password());
  }

  @Override
  public void create() {
    account.create();
    try {
      profile.create();
    } catch (RuntimeException exception) {
      try {
        account.delete();
      } catch (RuntimeException cleanupException) {
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
        .with(profile::delete)
        .with(account::delete)
        .close();
  }
}
