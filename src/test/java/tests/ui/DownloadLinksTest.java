package tests.ui;

import base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Hero düymələri, yükləmə bölməsi və footer linkləri (desktop görünüş). */
public class DownloadLinksTest extends BaseTest {

    private static final String APP_STORE_URL = "https://apps.apple.com/app/id6774554172";

    private static final By APP_STORE_LINK =
            By.xpath("//a[.//span[normalize-space()='App Store']]");
    private static final By HERO_APP_STORE = By.cssSelector(".hero-cta-group a.hero-btn");
    private static final By HERO_GOOGLE_PLAY = By.cssSelector(".hero-cta-group div.hero-btn");
    private static final By DOWNLOAD_TITLE = By.cssSelector(".download-title");
    private static final By QR_CODE = By.cssSelector(".download-qr-code svg");
    private static final By QR_TEXT = By.cssSelector(".download-qr-text");
    private static final By MOBILE_APP_STORE = By.cssSelector(".download-buttons-mobile a");
    private static final By FOOTER_STORE_LINKS = By.cssSelector(".footer-store-links a");
    private static final By INSTAGRAM_LINK = By.cssSelector("footer a[aria-label='Instagram']");
    private static final By LEGAL_TITLE = By.cssSelector("h1.legal-title");

    @Test
    public void appStoreButtonShouldBeVisible() {
        openHome();
        WebElement link = visible(APP_STORE_LINK);
        Assert.assertTrue(link.isDisplayed(), "App Store düyməsi görünməlidir");
    }

    @Test
    public void heroAppStoreButtonShouldLeadToDownloadSection() {
        openHome();

        WebElement appStore = visible(HERO_APP_STORE);
        Assert.assertTrue(appStore.getAttribute("href").endsWith("#download"));

        appStore.click();

        wait.until(ExpectedConditions.urlContains("#download"));
        WebElement section = driver.findElement(By.id("download"));
        wait.until(d -> isInViewport(section));
    }

    @Test
    public void googlePlayShouldBeMarkedAsComingSoon() {
        openHome();

        WebElement googlePlay = visible(HERO_GOOGLE_PLAY);
        Assert.assertTrue(googlePlay.getText().contains("Google Play"));
        Assert.assertTrue(googlePlay.getText().contains("Tezliklə"), "Google Play 'Tezliklə' kimi göstərilməlidir");
        Assert.assertNotEquals(googlePlay.getTagName(), "a", "Google Play hələ link olmamalıdır");
    }

    @Test
    public void downloadSectionShouldShowQrCode() {
        openHome();

        WebElement title = driver.findElement(DOWNLOAD_TITLE);
        scrollToCenter(title);

        Assert.assertEquals(visible(DOWNLOAD_TITLE).getText().trim(), "Tətbiqi yükləyin");
        Assert.assertTrue(visible(QR_CODE).isDisplayed(), "QR kod görünməlidir");
        Assert.assertEquals(visible(QR_TEXT).getText().trim(), "QR kodu skan edərək yükləyin");
    }

    @Test
    public void appStoreLinkShouldPointToAppleStore() {
        openHome();

        // Desktop-da bu düymə gizlidir (yalnız mobildə görünür), amma DOM-da olmalıdır
        WebElement link = wait.until(ExpectedConditions.presenceOfElementLocated(MOBILE_APP_STORE));
        Assert.assertEquals(link.getAttribute("href"), APP_STORE_URL);
        Assert.assertEquals(link.getAttribute("target"), "_blank", "Mağaza linki yeni tabda açılmalıdır");
    }

    @Test
    public void footerStoreLinksShouldLeadToDownloadSection() {
        openHome();

        for (WebElement link : driver.findElements(FOOTER_STORE_LINKS)) {
            Assert.assertEquals(link.getAttribute("href"), HOME_URL + "#download",
                    "Footer-dəki '" + link.getText().trim() + "' linki yükləmə bölməsinə aparmalıdır");
        }
    }

    @DataProvider(name = "legalPages")
    public Object[][] legalPages() {
        return new Object[][]{
                {"Məxfilik siyasəti", "/privacy", "Məxfilik siyasəti"},
                {"İstifadə şərtləri", "/terms", "İstifadə şərtləri"},
        };
    }

    @Test(dataProvider = "legalPages")
    public void footerLegalLinkShouldOpenPage(String linkText, String path, String expectedHeading) {
        openHome();

        clickWhenReady(By.xpath("//footer//a[normalize-space()='" + linkText + "']"));

        wait.until(ExpectedConditions.urlToBe(HOME_URL + path));
        Assert.assertTrue(visible(LEGAL_TITLE).getText().startsWith(expectedHeading),
                "Səhifə başlığı '" + expectedHeading + "' ilə başlamalıdır");
    }

    @Test
    public void instagramLinkShouldOpenInNewTab() {
        openHome();

        WebElement instagram = wait.until(ExpectedConditions.presenceOfElementLocated(INSTAGRAM_LINK));
        Assert.assertEquals(instagram.getAttribute("href"), "https://instagram.com/aypara.app");
        Assert.assertEquals(instagram.getAttribute("target"), "_blank");
        Assert.assertTrue(instagram.getAttribute("rel").contains("noopener"),
                "Xarici link rel='noopener' ilə olmalıdır");
    }
}
