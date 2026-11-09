package io.student.rangiffler.data;

import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.FriendshipStatus;
import io.student.rangiffler.data.db.Friendships;
import java.util.List;

/** Adds outgoing pending invitations around a delegated user fixture. */
public final class UserWithOutcomeInvitationsFixture implements UserFixture {
    private final UserFixture delegate;
    private final List<UserFixture> invitees;

    public UserWithOutcomeInvitationsFixture(UserFixture delegate) {
        this.delegate = delegate;
        invitees = delegate.user().outcomeInvitations().stream()
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
        for (UserFixture invitee : invitees) {
            invitee.create();
            new Friendships(user().username()).create(new Friendship(user().username(), invitee.user().username(), FriendshipStatus.PENDING));
        }
    }

    @Override
    public void close() {
        new TearDown()
                .withEach(invitees, invitee -> new Friendship(user().username(), invitee.user().username(), FriendshipStatus.PENDING).delete())
                .withEach(invitees, UserFixture::close)
                .with(delegate::close)
                .close();
    }
}
