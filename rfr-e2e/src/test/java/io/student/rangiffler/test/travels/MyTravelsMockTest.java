package io.student.rangiffler.test.travels;

import com.microsoft.playwright.Page;
import io.student.rangiffler.data.MockPhoto;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.UiTest;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.ui.page.MyTravelsPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UiTest
public class MyTravelsMockTest {
    @Test
    @DisplayName("Отображаются только мои путешествия на mock-ленте")
    void onlyMyTravelsShowsOwnPhotos(@User TestUser user, Page page) {
        TravelsLogin.open(page, user)
                .checkThatCardCount(4)
                .checkThatVisitedCountries("4 countries visited");
    }

    @Test
    @DisplayName("Отображаются путешествия с друзьями на mock-ленте")
    void withFriendsAddsFriendsPhotos(@User TestUser user, Page page) {
        TravelsLogin.open(page, user)
                .clickWithFriends()
                .checkThatCardCount(7)
                .checkThatVisitedCountries("7 countries visited");
    }

    @Test
    @DisplayName("Переход с путешествий с друзей на мои на mock-ленте")
    void onlyMyTravelsHidesFriendsPhotosAgain(@User TestUser user, Page page) {
        TravelsLogin.open(page, user)
                .clickWithFriends()
                .checkThatCardCount(7)
                .checkThatVisitedCountries("7 countries visited")
                .clickOnlyMyTravels()
                .checkThatCardCount(4)
                .checkThatVisitedCountries("4 countries visited");
    }

    @Test
    @DisplayName("Проверка фильтра и его сброс на mock-ленте")
    void countryFilterNarrowsFeedUntilReset(@User TestUser user, Page page) {
        TravelsLogin.open(page, user)
                .filterByCountry(MockPhoto.AMSTERDAM.country())
                .checkThatCardCount(1)
                .checkThatCountryFilter(MockPhoto.AMSTERDAM.country())
                .clickResetCountryFilter(MockPhoto.AMSTERDAM.country())
                .checkThatNoCountryFilter(MockPhoto.AMSTERDAM.country())
                .checkThatCardCount(4);
    }

    @Test
    @DisplayName("У моей карточки есть кнопка Actions, у карточки друзей нет на mock-ленте")
    void ownerMenuOnlyOnOwnPhotos(@User TestUser user, Page page) {
        MyTravelsPage myTravelsPage = TravelsLogin.open(page, user)
                .clickWithFriends()
                .checkThatCardCount(7);
        myTravelsPage.card(MockPhoto.AMSTERDAM.description())
                .checkThatOwnerMenuVisible();
        myTravelsPage.card(MockPhoto.THAILAND.description())
                .checkThatOwnerMenuAbsent();
    }

    @Test
    @DisplayName("Карточка отображает лайки на mock-ленте")
    void cardShowsMockLikes(@User TestUser user, Page page) {
        TravelsLogin.open(page, user)
                .checkThatCardCount(4)
                .card(MockPhoto.PARIS.description())
                .checkThatLikes("2 likes");
    }
}
