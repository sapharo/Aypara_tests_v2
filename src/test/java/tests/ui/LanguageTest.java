package tests.ui;

import base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

/** AZ / EN / RU dil keçidi. */
public class LanguageTest extends BaseTest {

    private static final By ACTIVE_LANGUAGE = By.cssSelector(".header-lang-link-item-active");
    private static final By NAV_LINKS = By.cssSelector("nav .header-nav-link");
    private static final By QUIZ_START_BUTTON = By.cssSelector(".quiz-section .premium-btn-primary");

    @DataProvider(name = "languages")
    public Object[][] languages() {
        // {kod, title, nav linkləri, quiz düyməsi}
        return new Object[][]{
                {"az", "Aypara AI | Sizin həyat bələdçiniz",
                        new String[]{"Özəlliklər", "Alətlər", "Yüklə"}, "Testə başla"},
                {"en", "Aypara AI | Your Life Guide",
                        new String[]{"Features", "Tools", "Download"}, "Start quiz"},
                {"ru", "Aypara AI | Ваш путеводитель по жизни",
                        new String[]{"Функции", "Инструменты", "Скачать"}, "Начать тест"},
        };
    }

    @Test(dataProvider = "languages")
    public void languageSwitcherShouldTranslatePage(String code, String title, String[] navTexts, String quizButton) {
        // Başqa dildən başlayırıq ki, keçid həqiqətən baş versin
        open(BASE_URL + ("az".equals(code) ? "/en" : "/az"));

        clickWhenReady(By.cssSelector(".header-lang-link-item[href='/" + code + "']"));

        wait.until(ExpectedConditions.urlToBe(BASE_URL + "/" + code));
        waitForSplashToDisappear();
        wait.until(ExpectedConditions.titleIs(title));

        Assert.assertEquals(driver.findElement(By.tagName("html")).getAttribute("lang"), code,
                "<html lang> atributu dilə uyğun olmalıdır");
        Assert.assertEquals(visible(ACTIVE_LANGUAGE).getText().trim(), code.toUpperCase(),
                "Aktiv dil düyməsi seçilmiş dil olmalıdır");

        List<WebElement> links = driver.findElements(NAV_LINKS);
        Assert.assertEquals(links.size(), navTexts.length);
        for (int i = 0; i < navTexts.length; i++) {
            Assert.assertEquals(links.get(i).getText().trim(), navTexts[i], "Nav linki tərcümə olunmalıdır");
        }

        WebElement start = driver.findElement(QUIZ_START_BUTTON);
        scrollToCenter(start);
        Assert.assertEquals(start.getText().trim(), quizButton, "Quiz düyməsi tərcümə olunmalıdır");
    }

    @Test
    public void footerLinksShouldKeepSelectedLanguage() {
        open(BASE_URL + "/en");

        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a.footer-link"));
        Assert.assertFalse(footerLinks.isEmpty());
        for (WebElement link : footerLinks) {
            Assert.assertTrue(link.getAttribute("href").startsWith(BASE_URL + "/en"),
                    "EN səhifəsində footer linkləri /en ilə başlamalıdır: " + link.getAttribute("href"));
        }
    }
}
