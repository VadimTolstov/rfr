package io.student.rangiffler.ui.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Locator.GetByRoleOptions;
import com.microsoft.playwright.Locator.GetByTextOptions;
import com.microsoft.playwright.options.AriaRole;
import java.util.List;
import java.util.Set;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Encapsulates the controls and relationship indicators inside a people row. */
final class PeopleRow {
  private final Locator row;

  PeopleRow(Locator row) {
    this.row = row;
  }

  void checkFriend() {
    checkRelationship(Set.of(Action.REMOVE), 0);
  }

  void checkIncomeInvitation() {
    checkRelationship(Set.of(Action.ACCEPT, Action.DECLINE), 0);
  }

  void checkOutcomeInvitation() {
    checkRelationship(Set.of(Action.CANCEL), 1);
  }

  void clickAdd() {
    Action.ADD.button(row).click();
  }

  void checkInvitationSent() {
    assertThat(Action.CANCEL.button(row)).hasCount(1);
  }

  private void checkRelationship(Set<Action> actions, int waiting) {
    for (Action action : List.of(Action.REMOVE, Action.ACCEPT, Action.DECLINE, Action.CANCEL)) {
      assertThat(action.button(row)).hasCount(actions.contains(action) ? 1 : 0);
    }
    assertThat(row.getByText("Waiting...", new GetByTextOptions().setExact(true))).hasCount(waiting);
  }

  /** Maps relationship actions to their accessible button names. */
  enum Action {
    ADD("Add"),
    REMOVE("Remove"),
    ACCEPT("Accept"),
    DECLINE("Decline"),
    CANCEL("Cancel");

    private final String label;

    Action(String label) {
      this.label = label;
    }

    Locator button(Locator scope) {
      return scope.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName(label).setExact(true));
    }
  }
}
