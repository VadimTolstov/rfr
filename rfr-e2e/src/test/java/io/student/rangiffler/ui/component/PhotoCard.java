package io.student.rangiffler.ui.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;

/**
 * Encapsulates the controls and counters of a single travel photo.
 */
public final class PhotoCard {
    private final Locator card;
    private final String description;

    public PhotoCard(Locator card, String description) {
        this.card = card;
        this.description = description;
    }

    @Step("Проверить наличие кнопки Photo actions у карточки «{this.description}»")
    public PhotoCard checkThatOwnerMenuVisible() {
        PlaywrightAssertions.assertThat(ownerMenu())
                .isVisible();
        return this;
    }

    @Step("Проверить отсутствие кнопки Photo actions у карточки «{this.description}»")
    public PhotoCard checkThatOwnerMenuAbsent() {
        PlaywrightAssertions.assertThat(card).isVisible();
        PlaywrightAssertions.assertThat(ownerMenu())
                .hasCount(0);
        return this;
    }

    @Step("Проверить счётчик лайков у карточки «{this.description}»: {likes}")
    public PhotoCard checkThatLikes(String likes) {
        PlaywrightAssertions.assertThat(card.getByTestId("photo-likes"))
                .hasText(likes);
        return this;
    }

    private Locator ownerMenu() {
        return card.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Photo actions").setExact(true));
    }
}
