package io.student.rangiffler.jupiter.request;

import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.data.SingleUserFixture;
import io.student.rangiffler.data.UserFixture;
import io.student.rangiffler.data.UserWithFriendsFixture;
import io.student.rangiffler.data.UserWithIncomeInvitations;
import java.util.List;
import java.util.stream.Stream;

/** Holds data and fixture configuration for one test user. */
public record UserRequest(int friends, int incomeInvitations) {
    public UserRequest {
        if (friends < 0) {
            throw new IllegalArgumentException("Friends count must not be negative");
        }
        if (incomeInvitations < 0) {
            throw new IllegalArgumentException("Income invitations count must not be negative");
        }
    }

    public UserFixture fixture() {
        TestUser credentials = TestUser.unique();
        TestUser user = new TestUser(credentials.username(), credentials.password(), users(friends), users(incomeInvitations));
        UserFixture fixture = new SingleUserFixture(user);
        if (!user.friends().isEmpty()) {
            fixture = new UserWithFriendsFixture(fixture);
        }
        if (!user.incomeInvitations().isEmpty()) {
            fixture = new UserWithIncomeInvitations(fixture);
        }
        return fixture;
    }

    private List<TestUser> users(int count) {
        return Stream.generate(TestUser::unique)
                .limit(count)
                .toList();
    }
}
