package tests.ui;

import base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Header naviqasiyası, səhifə başlığı, yönləndirmələr və 404. */
public class NavigationTest extends BaseTest {

    private static final By LOGO_LINK = By.cssSelector("a.header-logo-link");
    private static final By NOT_FOUND_HEADING = By.cssSelector("h1.next-error-h1");

    @Test
    public void homePageShouldHaveCorrectTitle() {
        openHome();
        Assert.assertEquals(driver.getTitle(), "Aypara AI | Sizin həyat bələdçiniz");
    }

    @Test
    public void rootUrlShouldRedirectToAzerbaijani() {
        open(BASE_URL + "/");
        wait.until(ExpectedConditions.urlToBe(HOME_URL));
    }

    @DataProvider(name = "navLinks")
    public Object[][] navLinks() {
        return new Object[][]{
                {"Özəlliklər", "features"},
                {"Alətlər", "tools"},
                {"Yüklə", "download"},
        };
    }

    @Test(dataProvider = "navLinks")
    public void headerLinkShouldScrollToSection(String linkText, String sectionId) {
        openHome();

        WebElement section = driver.findElement(By.id(sectionId));
        Assert.assertFalse(isScrolledBelowHeader(section), "Klikdən əvvəl bölmə yuxarıda olmamalıdır");

        // Linklər URL-ə #hash yazmır: JS ilə smooth scroll edir
        clickWhenReady(By.xpath("//nav//a[contains(@class,'header-nav-link')][normalize-space()='" + linkText + "']"));

        // Son bölmələr (məs. "Yüklə") hündür ekranda header-in altına qədər qalxa bilmir —
        // səhifə sona çatır. O halda səhifənin sonuna scroll olunması və bölmənin görünməsi kifayətdir.
        wait.until(d -> isScrolledBelowHeader(section) || (isScrolledToBottom() && isInViewport(section)));
    }

    private boolean isScrolledToBottom() {
        return (Boolean) js("return window.innerHeight + window.scrollY >= "
                + "document.documentElement.scrollHeight - 2;");
    }

    @Test
    public void logoShouldLeadToHomePage() {
        open(HOME_URL + "/privacy");

        clickWhenReady(LOGO_LINK);

        wait.until(ExpectedConditions.urlToBe(HOME_URL));
    }

    @Test
    public void unknownPageShouldShow404() {
        driver.get(HOME_URL + "/olmayan-sehife");

        Assert.assertEquals(visible(NOT_FOUND_HEADING).getText().trim(), "404");
    }

    @Test
    public void unsupportedLanguageShouldShow404() {
        // /de dəstəklənmir: brauzerin dilinə görə /az/de, /en/de və ya /ru/de-yə yönləndirilir və 404 qaytarır
        driver.get(BASE_URL + "/de");

        wait.until(ExpectedConditions.urlMatches("^" + BASE_URL + "/(az|en|ru)/de$"));
        Assert.assertEquals(visible(NOT_FOUND_HEADING).getText().trim(), "404");
    }
}
