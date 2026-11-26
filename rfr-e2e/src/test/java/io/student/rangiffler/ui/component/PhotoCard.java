package io.student.rangiffler.ui.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;

import java.util.regex.Pattern;

public final class PhotoCard {
    private final Locator card;
    private final String description;

    public PhotoCard(Locator card, String description) {
        this.card = card;
        this.description = description;
    }

    @Step("Кнопка \"Photo actions\" видима")
    public PhotoCard checkThatOwnerMenuVisible() {
        PlaywrightAssertions.assertThat(ownerMenu())
                .isVisible();
        return this;
    }

    @Step("Кнопка \"Photo actions\" отсутствует")
    public PhotoCard  checkThatOwnerMenuAbsent() {
        PlaywrightAssertions.assertThat(ownerMenu())
                .hasCount(0);
        return this;
    }

    @Step("Проверить, что счётчик лайков показывает {likes}")
    public PhotoCard checkThatLikes(String likes) {
        PlaywrightAssertions.assertThat(card.locator("button[aria-label='like'] + *"))
                .hasText(likes);
        return this;
    }

    private Locator ownerMenu() {
        return card.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Photo actions").setExact(true));
    }
}
