package tests;

import base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AyparaTest extends BaseTest {

    private static final String URL = "https://aypara.app/az";
    private static final By APP_STORE_LINK =
            By.xpath("//a[.//span[normalize-space()='App Store']]");

    @Test
    public void appStoreButtonShouldBeVisible() {
        driver.get(URL);
        WebElement link = wait.until(ExpectedConditions.visibilityOfElementLocated(APP_STORE_LINK));
        Assert.assertTrue(link.isDisplayed(), "App Store düyməsi görünməlidir");
    }
}