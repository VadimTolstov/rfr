package io.student.rangiffler.data;

import io.student.rangiffler.jupiter.annotation.User;
import java.util.List;
import java.util.stream.Stream;

/** Holds data and fixture configuration for one test user. */
public record UserRequest(int friends, int incomeInvitations, int outcomeInvitations) {
    public UserRequest(User user) {
        this(user.friends(), user.incomeInvitations(), user.outcomeInvitations());
    }

    public UserRequest {
        if (friends < 0) {
            throw new IllegalArgumentException("Friends count must not be negative");
        }
        if (incomeInvitations < 0) {
            throw new IllegalArgumentException("Income invitations count must not be negative");
        }
        if (outcomeInvitations < 0) {
            throw new IllegalArgumentException("Outcome invitations count must not be negative");
        }
    }

    public UserFixture fixture() {
        TestUser credentials = TestUser.unique();
        TestUser user = new TestUser(credentials.username(), credentials.password(), users(friends), users(incomeInvitations), List.of())
                .withOutcomeInvitations(users(outcomeInvitations));
        UserFixture fixture = new SingleUserFixture(user);
        for (Relation relation : Relation.values()) {
            if (!relation.users(user).isEmpty()) {
                fixture = new UserWithRelationsFixture(fixture, relation);
            }
        }
        return fixture;
    }

    private List<TestUser> users(int count) {
        return Stream.generate(TestUser::unique)
                .limit(count)
                .toList();
    }
}
