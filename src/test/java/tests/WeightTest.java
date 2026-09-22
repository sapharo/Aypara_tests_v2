package tests;

import base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class WeightTest extends BaseTest {

    private static final String URL = "https://aypara.app/az";

    private static final By SPLASH_SCREEN = By.cssSelector(".splash-screen");
    private static final By PLUS_BUTTON = By.xpath(
            "//button[contains(@class,'weight-adjust-btn')][normalize-space()='+']");
    private static final By SAVE_BUTTON = By.xpath(
            "//button[@type='submit'][.//span[normalize-space()='Yadda saxla']]");
    private static final By WEIGHT_VALUE = By.cssSelector(".weight-value-number");

    @Test
    public void shouldIncreaseWeightOnceAndPersistAfterSave() {
        driver.get(URL);
        waitForSplashToDisappear();

        double weightBefore = readWeightValue();

        WebElement plus = wait.until(ExpectedConditions.elementToBeClickable(PLUS_BUTTON));
        scrollToCenter(plus);
        plus.click();

        double weightAfterClick = readWeightValue();
        Assert.assertEquals(weightAfterClick, weightBefore + 0.1, 0.001,
                "+ düyməsindən sonra dəyər 0.1 vahid artmalıdır");

        WebElement save = wait.until(ExpectedConditions.elementToBeClickable(SAVE_BUTTON));
        scrollToCenter(save);
        save.click();

        // Yadda saxlandığını təsdiqləmək üçün səhifəni yenilə
        driver.navigate().refresh();
        waitForSplashToDisappear();

        double weightAfterRefresh = readWeightValue();
        Assert.assertEquals(weightAfterRefresh, weightBefore + 0.1, 0.001,
                "Refresh-dən sonra dəyər saxlanmalıdır (backend-ə yazılmalıdır)");
    }

    private double readWeightValue() {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(WEIGHT_VALUE));
        String text = el.getText().replace(",", ".").trim(); // vergüllü format ehtimalına qarşı
        return Double.parseDouble(text);
    }

    private void waitForSplashToDisappear() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(SPLASH_SCREEN));
    }

    private void scrollToCenter(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", element);
    }
}