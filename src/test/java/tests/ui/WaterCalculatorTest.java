package tests.ui;

import base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * "Su ehtiyacı kalkulyatoru" tərəzidə saxlanılan son çəkidən hesablanır:
 * litr = round(çəki × 0.035, 1), stəkan sayı = round(litr × 1000 / 250).
 */
public class WaterCalculatorTest extends BaseTest {

    private static final By PLACEHOLDER_TEXT = By.cssSelector(".water-calc-placeholder-text");
    private static final By RESULT_VALUE = By.cssSelector(".water-calc-result-value");
    private static final By RECOMMENDATION = By.cssSelector(".water-calc-recommendation");
    private static final By SAVE_BUTTON = By.xpath(
            "//button[@type='submit'][.//span[normalize-space()='Yadda saxla']]");
    private static final By SLIDER = By.cssSelector("input.weight-ruler-input");
    private static final By WEIGHT_VALUE = By.cssSelector(".weight-value-number");
    private static final By CLEAR_HISTORY_BUTTON = By.cssSelector(".clear-history-btn");

    @Test
    public void placeholderShouldBeShownBeforeWeightIsSaved() {
        openHome();

        Assert.assertEquals(visible(PLACEHOLDER_TEXT).getText().trim(),
                "Su balansını hesablamaq üçün çəkini aşağıdakı tərəzidə qeyd edin");
        Assert.assertTrue(driver.findElements(RESULT_VALUE).isEmpty(),
                "Çəki saxlanmayıbsa nəticə göstərilməməlidir");
    }

    @Test
    public void shouldCalculateWaterNeedFor75Kg() {
        openHome();

        clickWhenReady(SAVE_BUTTON); // başlanğıc dəyər 75.0 kq

        assertWaterResult(2.6, 10);
    }

    @Test
    public void shouldRecalculateWhenWeightChanges() {
        openHome();

        // 75.0 → 80.0 kq (50 addım × 0.1)
        WebElement slider = wait.until(ExpectedConditions.presenceOfElementLocated(SLIDER));
        scrollToCenter(slider);
        for (int i = 0; i < 50; i++) {
            slider.sendKeys(Keys.ARROW_RIGHT);
        }
        wait.until(ExpectedConditions.textToBe(WEIGHT_VALUE, "80.0"));

        clickWhenReady(SAVE_BUTTON);

        // 80 × 0.035 = 2.8 litr → 2800 / 250 = 11.2 → 11 stəkan
        assertWaterResult(2.8, 11);
    }

    @Test
    public void resultShouldPersistAfterRefresh() {
        openHome();
        clickWhenReady(SAVE_BUTTON);
        visible(RESULT_VALUE);

        refreshPage();

        assertWaterResult(2.6, 10);
    }

    @Test
    public void clearingHistoryShouldResetCalculator() {
        openHome();
        clickWhenReady(SAVE_BUTTON);
        visible(RESULT_VALUE);

        clickWhenReady(CLEAR_HISTORY_BUTTON);

        visible(PLACEHOLDER_TEXT);
        Assert.assertTrue(driver.findElements(RESULT_VALUE).isEmpty(),
                "Tarixçə təmizlənəndən sonra nəticə gizlənməlidir");
    }

    private void assertWaterResult(double expectedLiters, int expectedGlasses) {
        WebElement result = visible(RESULT_VALUE);
        // Mətn "2.6 litr" formatındadır
        double liters = Double.parseDouble(result.getText().trim().split("\\s+")[0].replace(",", "."));
        Assert.assertEquals(liters, expectedLiters, 0.001, "Gündəlik su ehtiyacı düzgün hesablanmalıdır");
        Assert.assertTrue(result.getText().contains("litr"), "Vahid 'litr' olmalıdır");

        String recommendation = visible(RECOMMENDATION).getText();
        Assert.assertTrue(recommendation.contains("250 ml"), "Tövsiyədə 250 ml qeyd olunmalıdır");
        Assert.assertTrue(recommendation.contains(String.valueOf(expectedGlasses)),
                "Tövsiyədə " + expectedGlasses + " dəfə yazılmalıdır, mətn: " + recommendation);
    }
}
