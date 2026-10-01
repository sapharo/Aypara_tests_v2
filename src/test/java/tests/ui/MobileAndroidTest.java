package tests.ui;

import base.MobileBaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Android emulyasiyası: yükləmə bölməsində yalnız Google Play ("Tezliklə") göstərilir. */
public class MobileAndroidTest extends MobileBaseTest {

    private static final By MOBILE_DOWNLOAD_ICON = By.cssSelector("header a.header-mobile-download");
    private static final By MOBILE_BUTTONS = By.cssSelector(".download-buttons-mobile");
    private static final By MOBILE_APP_STORE = By.cssSelector(".download-buttons-mobile a");
    private static final By MOBILE_GOOGLE_PLAY = By.cssSelector(".download-buttons-mobile div.btn");

    @Override
    protected String userAgent() {
        return ANDROID_USER_AGENT;
    }

    @Test
    public void androidShouldSeeOnlyGooglePlayComingSoon() {
        openHome();

        WebElement buttons = wait.until(ExpectedConditions.presenceOfElementLocated(MOBILE_BUTTONS));
        scrollToCenter(buttons);

        WebElement googlePlay = visible(MOBILE_GOOGLE_PLAY);
        Assert.assertTrue(googlePlay.getText().contains("Google Play"));
        Assert.assertTrue(googlePlay.getText().contains("Tezliklə"), "Google Play 'Tezliklə' kimi göstərilməlidir");
        Assert.assertTrue(driver.findElements(MOBILE_APP_STORE).isEmpty(),
                "Android-da App Store düyməsi göstərilməməlidir");
    }

    @Test
    public void mobileDownloadIconShouldLinkToGooglePlay() {
        openHome();

        // Qeyd: hazırda tətbiqin səhifəsinə yox, Google Play-in ana səhifəsinə aparır (tətbiq hələ "Tezliklə")
        WebElement icon = visible(MOBILE_DOWNLOAD_ICON);
        wait.until(ExpectedConditions.attributeContains(icon, "href", "https://play.google.com/store"));
    }
}
