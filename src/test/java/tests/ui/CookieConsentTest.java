package tests.ui;

import base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Cookie razılığı banneri. Seçim localStorage-da ("cookie_consent") saxlanır.
 * Burada open() istifadə etmirik, çünki o, banneri avtomatik bağlayır.
 */
public class CookieConsentTest extends BaseTest {

    private static final String CONSENT_KEY = "cookie_consent";

    private static final By BANNER = By.cssSelector(".cookie-consent-box");
    private static final By MESSAGE = By.cssSelector(".cookie-consent-box .cookie-message");
    private static final By ACCEPT_BUTTON = By.cssSelector(".cookie-consent-box .cookie-accept");
    private static final By DECLINE_BUTTON = By.cssSelector(".cookie-consent-box .cookie-decline");

    @Test
    public void bannerShouldBeShownOnFirstVisit() {
        openWithoutDismissingCookies();

        Assert.assertTrue(visible(MESSAGE).getText().startsWith("Biz sayt təcrübənizi inkişaf etdirmək üçün Cookie"));
        Assert.assertEquals(visible(ACCEPT_BUTTON).getText().trim(), "Qəbul edirəm");
        Assert.assertEquals(visible(DECLINE_BUTTON).getText().trim(), "İmtina edirəm");
    }

    @Test
    public void declineShouldHideBannerAndRememberChoice() {
        openWithoutDismissingCookies();

        clickWhenReady(DECLINE_BUTTON);

        wait.until(ExpectedConditions.invisibilityOfElementLocated(BANNER));
        Assert.assertEquals(localStorageItem(CONSENT_KEY), "false");
        assertBannerNotShownAfterRefresh();
    }

    @Test
    public void acceptShouldHideBannerAndRememberChoice() {
        openWithoutDismissingCookies();

        clickWhenReady(ACCEPT_BUTTON);

        wait.until(ExpectedConditions.invisibilityOfElementLocated(BANNER));
        Assert.assertEquals(localStorageItem(CONSENT_KEY), "true");
        assertBannerNotShownAfterRefresh();
    }

    private void openWithoutDismissingCookies() {
        driver.get(HOME_URL);
        waitForSplashToDisappear();
        visible(BANNER);
    }

    private void assertBannerNotShownAfterRefresh() {
        refreshPage();
        Assert.assertTrue(driver.findElements(BANNER).isEmpty(), "Seçim edildikdən sonra banner yenidən çıxmamalıdır");
    }
}
