package io.student.rangiffler.data;

import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.FriendshipStatus;
import io.student.rangiffler.data.db.Friendships;
import java.util.List;

/** Adds incoming pending invitations around a delegated user fixture. */
public final class UserWithIncomeInvitations implements UserFixture {
    private final UserFixture delegate;
    private final List<UserFixture> inviters;

    public UserWithIncomeInvitations(UserFixture delegate) {
        this.delegate = delegate;
        inviters = delegate.user().incomeInvitations().stream()
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
        for (UserFixture inviter : inviters) {
            inviter.create();
            new Friendships(user().username()).create(new Friendship(inviter.user().username(), user().username(), FriendshipStatus.PENDING));
        }
    }

    @Override
    public void close() {
        new TearDown()
            .withEach(inviters, inviter -> new Friendship(inviter.user().username(), user().username(), FriendshipStatus.PENDING).delete())
            .withEach(inviters, UserFixture::close)
            .with(delegate::close)
            .close();
    }

}
