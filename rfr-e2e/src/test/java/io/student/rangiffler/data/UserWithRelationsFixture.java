package io.student.rangiffler.data;

import io.student.rangiffler.data.db.Friendships;
import java.util.List;

/** Adds related users and their friendship rows around a delegated fixture. */
public final class UserWithRelationsFixture implements UserFixture {
  private final UserFixture delegate;
  private final Relation relation;
  private final List<UserFixture> related;

  public UserWithRelationsFixture(UserFixture delegate, Relation relation) {
    this.delegate = delegate;
    this.relation = relation;
    related = relation.users(delegate.user()).stream()
        .<UserFixture>map(SingleUserFixture::new)
        .toList();
  }

  @Override
  public TestUser user() {
    return delegate.user();
  }

  @Override
  public void create() {
    delegate.create();
    for (UserFixture fixture : related) {
      fixture.create();
      new Friendships(user().username()).create(relation.friendship(user(), fixture.user()));
    }
  }

  @Override
  public void close() {
    new TearDown()
        .withEach(related, fixture -> relation.friendship(user(), fixture.user()).delete())
        .withEach(related, UserFixture::close)
        .with(delegate::close)
        .close();
  }
}
