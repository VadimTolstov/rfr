package io.student.rangiffler.data;

import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.FriendshipStatus;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/** Describes the related users and friendship direction for a fixture relation. */
public enum Relation {
  FRIEND(TestUser::friends, (origin, related) -> new Friendship(origin.username(), related.username(), FriendshipStatus.ACCEPTED)),
  INCOME_INVITATION(TestUser::incomeInvitations, (origin, related) -> new Friendship(related.username(), origin.username(), FriendshipStatus.PENDING)),
  OUTCOME_INVITATION(TestUser::outcomeInvitations, (origin, related) -> new Friendship(origin.username(), related.username(), FriendshipStatus.PENDING));

  private final Function<TestUser, List<TestUser>> users;
  private final BiFunction<TestUser, TestUser, Friendship> friendship;

  Relation(Function<TestUser, List<TestUser>> users, BiFunction<TestUser, TestUser, Friendship> friendship) {
    this.users = users;
    this.friendship = friendship;
  }

  List<TestUser> users(TestUser origin) {
    return users.apply(origin);
  }

  Friendship friendship(TestUser origin, TestUser related) {
    return friendship.apply(origin, related);
  }
}
