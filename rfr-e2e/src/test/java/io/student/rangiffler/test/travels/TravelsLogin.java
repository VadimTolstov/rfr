package io.student.rangiffler.test.travels;

import com.microsoft.playwright.Page;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.ui.page.LoginPage;
import io.student.rangiffler.ui.page.MyTravelsPage;

/** Opens the travel feed after signing in through the browser. */
final class TravelsLogin {

    static MyTravelsPage open(Page page, TestUser user) {
        new LoginPage(page)
                .open()
                .clickLogin()
                .fillLoginForm(user.username(), user.password())
                .clickSignIn();
        page.waitForURL(MyTravelsPage.URL);
        return new MyTravelsPage(page);
    }
}
