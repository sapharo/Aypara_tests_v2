package tests.ui;

import base.MobileBaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

/** iPhone emulyasiyası: mobil header, yalnız App Store düyməsi, tema həmişə açıq. */
public class MobileIosTest extends MobileBaseTest {

    private static final String APP_STORE_URL = "https://apps.apple.com/app/id6774554172";

    private static final By MOBILE_DOWNLOAD_ICON = By.cssSelector("header a.header-mobile-download");
    private static final By DESKTOP_NAV = By.cssSelector("nav.header-nav-links");
    private static final By MOBILE_APP_STORE = By.cssSelector(".download-buttons-mobile a");
    private static final By MOBILE_GOOGLE_PLAY = By.cssSelector(".download-buttons-mobile div.btn");
    private static final By THEME_TOGGLE = By.cssSelector("header button[aria-label='Toggle theme']");
    private static final By HTML = By.tagName("html");

    @Override
    protected String userAgent() {
        return IPHONE_USER_AGENT;
    }

    @Test
    public void headerShouldShowMobileDownloadIconInsteadOfNav() {
        openHome();

        Assert.assertTrue(visible(MOBILE_DOWNLOAD_ICON).isDisplayed(), "Mobil 'Download' ikonu görünməlidir");
        Assert.assertFalse(driver.findElement(DESKTOP_NAV).isDisplayed(), "Desktop nav mobildə gizli olmalıdır");
    }

    @Test
    public void mobileDownloadIconShouldLinkToAppStore() {
        openHome();

        // iPhone-da header ikonu birbaşa App Store-a aparır (Android-da Google Play, desktop-da #download)
        WebElement icon = visible(MOBILE_DOWNLOAD_ICON);
        wait.until(ExpectedConditions.attributeToBe(icon, "href", APP_STORE_URL));
    }

    @Test
    public void iphoneShouldSeeOnlyAppStoreButton() {
        openHome();

        WebElement appStore = wait.until(ExpectedConditions.presenceOfElementLocated(MOBILE_APP_STORE));
        scrollToCenter(appStore);

        Assert.assertTrue(appStore.isDisplayed(), "iPhone-da App Store düyməsi görünməlidir");
        Assert.assertEquals(appStore.getAttribute("href"), APP_STORE_URL);
        Assert.assertTrue(driver.findElements(MOBILE_GOOGLE_PLAY).isEmpty(),
                "iPhone-da Google Play düyməsi göstərilməməlidir");
    }

    @Test
    public void themeShouldStayLightOnMobile() {
        openHome();
        Assert.assertEquals(driver.findElement(HTML).getAttribute("data-theme"), "light");

        WebElement toggle = driver.findElement(THEME_TOGGLE);
        if (toggle.isDisplayed()) {
            toggle.click();
        }

        Assert.assertEquals(driver.findElement(HTML).getAttribute("data-theme"), "light",
                "Mobildə tema həmişə açıq qalmalıdır");
    }
}
