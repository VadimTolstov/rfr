package io.student.rangiffler.test.travels;

import com.microsoft.playwright.Page;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.ui.page.LoginPage;
import io.student.rangiffler.ui.page.MyTravelsPage;

final class TravelsLogin {

    static MyTravelsPage open(Page page, TestUser user) {
        return new LoginPage(page)
                .open()
                .clickLogin()
                .fillLoginForm(user.username(), user.password())
                .clickSignIn()
                .clickMyMap();
    }
}
