package io.student.rangiffler.data;

import io.student.rangiffler.utils.RandomDataUtils;
import java.util.List;

/** Carries credentials and related test users. */
public record TestUser(String username, String password, List<TestUser> friends, List<TestUser> incomeInvitations, List<TestUser> outcomeInvitations) {
  public TestUser {
    friends = List.copyOf(friends);
    incomeInvitations = List.copyOf(incomeInvitations);
    outcomeInvitations = List.copyOf(outcomeInvitations);
  }

  public TestUser withOutcomeInvitations(List<TestUser> outcomeInvitations) {
    return new TestUser(username, password, friends, incomeInvitations, outcomeInvitations);
  }

  public static TestUser unique() {
    return new TestUser(RandomDataUtils.randomUsername(), RandomDataUtils.randomPassword(), List.of(), List.of(), List.of());
  }
}
