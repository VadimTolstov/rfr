package io.student.rangiffler.ui.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.ui.component.PhotoCard;

import java.util.regex.Pattern;

public final class MyTravelsPage {
    public static final String URL = TestConfig.frontUrl() + "/my-travels";

    private final Page page;
    private final Locator cards;
    private final Locator onlyMyTravels;
    private final Locator withFriends;
    private final Locator visitedCountries;
    private final Locator countries;
    private final Locator chipCountry;


    public MyTravelsPage(Page page) {
        this.page = page;
        cards = page.getByRole(AriaRole.ARTICLE);
        onlyMyTravels = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Only my travels").setExact(true));
        withFriends = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("With friends").setExact(true));
        visitedCountries = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("^[0-9]+ countr(?:y|ies) visited$")));
        countries = page.getByRole(AriaRole.TOOLTIP);
        chipCountry = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("^Country filter: .+$")));
    }

    @Step("Проверить, что отображается {count} карточек фото")
    public MyTravelsPage checkThatCardCount(int count) {
        PlaywrightAssertions.assertThat(cards).hasCount(count);
        return this;
    }


    @Step("Отобразить только мои путешествия")
    public MyTravelsPage clickOnlyMyTravels() {
        onlyMyTravels.click();
        return this;
    }

    @Step("Отобразить путешествия с друзьями")
    public MyTravelsPage clickWithFriends() {
        withFriends.click();
        return this;
    }

    @Step("Отобразить путешествия в {country}")
    public MyTravelsPage filterByCountry(String country) {
        visitedCountries.click();
        countries.getByText(country, new Locator.GetByTextOptions().setExact(true)).click();
        return this;
    }

    private Locator countryFilter(String country) {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Country filter: " + country).setExact(true));
    }

    @Step("Выбран фильтр страны {country}")
    public MyTravelsPage checkThatCountryFilter(String country) {
        PlaywrightAssertions.assertThat(countryFilter(country)).isVisible();
        return this;
    }

    @Step("Фильтр страны {country} отключен")
    public MyTravelsPage checkThatNoCountryFilter(String country) {
        PlaywrightAssertions.assertThat(countryFilter(country)).hasCount(0);
        return this;
    }

    @Step("Отключить фильтр по стране {country}")
    public MyTravelsPage clickResetCountryFilter(String country) {
        countryFilter(country)
                .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Reset country filter").setExact(true))
                .click();
        return this;
    }

    @Step("Проверяем, что текст отображения стран соответствует {text}")
    public MyTravelsPage checkThatVisitedCountries(String text) {
        PlaywrightAssertions.assertThat(visitedCountries).hasText(text);
        return this;
    }

    @Step("Открыть карточку с описанием {description}")
    public PhotoCard card(String description){
        Locator filter = cards.filter(new Locator.FilterOptions().setHas(
                page.getByText(description, new Page.GetByTextOptions().setExact(true))
        ));
        return new PhotoCard(filter, description);
    }
}
