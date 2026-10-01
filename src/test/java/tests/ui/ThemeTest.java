package tests.ui;

import base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Gecə/gündüz rejimi (yalnız desktop). Seçim localStorage-da ("theme") saxlanır,
 * cari tema &lt;html data-theme&gt; atributundadır. Mobil davranış MobileIosTest-dədir.
 */
public class ThemeTest extends BaseTest {

    private static final By THEME_TOGGLE = By.cssSelector("header button[aria-label='Toggle theme']");
    private static final By HTML = By.tagName("html");

    @Test
    public void defaultThemeShouldBeLight() {
        openHome();
        Assert.assertEquals(currentTheme(), "light");
    }

    @Test
    public void toggleShouldSwitchBetweenLightAndDark() {
        openHome();

        clickWhenReady(THEME_TOGGLE);
        wait.until(ExpectedConditions.attributeToBe(HTML, "data-theme", "dark"));
        Assert.assertEquals(localStorageItem("theme"), "dark");

        clickWhenReady(THEME_TOGGLE);
        wait.until(ExpectedConditions.attributeToBe(HTML, "data-theme", "light"));
        Assert.assertEquals(localStorageItem("theme"), "light");
    }

    @Test
    public void tooltipShouldDescribeNextTheme() {
        openHome();
        Assert.assertEquals(driver.findElement(THEME_TOGGLE).getAttribute("data-tooltip"), "Gecə rejimi");

        clickWhenReady(THEME_TOGGLE);

        wait.until(ExpectedConditions.not(
                ExpectedConditions.attributeToBe(THEME_TOGGLE, "data-tooltip", "Gecə rejimi")));
    }

    @Test
    public void darkThemeShouldPersistAfterRefresh() {
        openHome();
        clickWhenReady(THEME_TOGGLE);
        wait.until(ExpectedConditions.attributeToBe(HTML, "data-theme", "dark"));

        refreshPage();

        wait.until(ExpectedConditions.attributeToBe(HTML, "data-theme", "dark"));
    }

    private String currentTheme() {
        return driver.findElement(HTML).getAttribute("data-theme");
    }
}
