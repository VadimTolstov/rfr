package io.student.rangiffler.data;

import io.student.rangiffler.utils.RandomDataUtils;
import java.util.List;

/** Carries credentials and related test users. */
public record TestUser(String username, String password, List<TestUser> friends, List<TestUser> incomeInvitations) {
  public TestUser {
    friends = List.copyOf(friends);
    incomeInvitations = List.copyOf(incomeInvitations);
  }

  public static TestUser unique() {
    return new TestUser(RandomDataUtils.randomUsername(), RandomDataUtils.randomPassword(), List.of(), List.of());
  }
}
