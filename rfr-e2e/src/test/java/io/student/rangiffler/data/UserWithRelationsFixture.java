package io.student.rangiffler.data;

import io.student.rangiffler.data.db.Friendship;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Adds related users and their friendship rows around a delegated fixture. */
public final class UserWithRelationsFixture implements UserFixture {
  private final UserFixture delegate;
  private final Relation relation;
  private final List<UserFixture> related;
  private final List<Runnable> friendships = new ArrayList<>();

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
      Friendship friendship = relation.friendship(user(), fixture.user());
      UUID id = UUID.randomUUID();
      friendship.create(id);
      friendships.add(() -> friendship.delete(id));
    }
  }

  @Override
  public void close() {
    new TearDown()
        .withEach(List.copyOf(friendships.reversed()), this::closeFriendship)
        .withEach(related.reversed(), UserFixture::close)
        .with(delegate::close)
        .close();
  }

  private void closeFriendship(Runnable friendship) {
    friendship.run();
    friendships.remove(friendship);
  }
}
