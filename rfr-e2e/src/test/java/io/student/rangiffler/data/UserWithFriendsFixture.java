package io.student.rangiffler.data;

import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.FriendshipStatus;
import io.student.rangiffler.data.db.Friendships;
import java.util.List;

/** Adds accepted friendships around a delegated user fixture. */
public final class UserWithFriendsFixture implements UserFixture {
    private final UserFixture delegate;
    private final List<UserFixture> friends;

    public UserWithFriendsFixture(UserFixture delegate) {
        this.delegate = delegate;
        friends = delegate.user().friends().stream()
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
        for (UserFixture friend : friends) {
            friend.create();
            new Friendships(user().username()).create(new Friendship(user().username(), friend.user().username(), FriendshipStatus.ACCEPTED));
        }
    }

    @Override
    public void close() {
        new TearDown()
            .withEach(friends, friend -> new Friendship(user().username(), friend.user().username(), FriendshipStatus.ACCEPTED).delete())
            .withEach(friends, UserFixture::close)
            .with(delegate::close)
            .close();
    }

}
