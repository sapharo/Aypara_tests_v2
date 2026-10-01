package tests.ui;

import base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * "Tərəzi" aləti. Dəyər 30–150 kq arasında, 0.1 addımla dəyişir.
 * Tarixçə brauzerin localStorage-ında ("aypara_weight_history") saxlanır, maksimum 5 qeyd.
 */
public class WeightTest extends BaseTest {

    private static final String HISTORY_KEY = "aypara_weight_history";

    private static final By PLUS_BUTTON = By.xpath(
            "//button[contains(@class,'weight-adjust-btn')][normalize-space()='+']");
    private static final By MINUS_BUTTON = By.xpath(
            "//button[contains(@class,'weight-adjust-btn')][normalize-space()='−']");
    private static final By SAVE_BUTTON = By.xpath(
            "//button[@type='submit'][.//span[normalize-space()='Yadda saxla']]");
    private static final By WEIGHT_VALUE = By.cssSelector(".weight-value-number");
    private static final By SLIDER = By.cssSelector("input.weight-ruler-input");
    private static final By HISTORY_ITEMS = By.cssSelector(".weight-history-item");
    private static final By HISTORY_ITEM_WEIGHT = By.cssSelector(".weight-record-weight");
    private static final By EMPTY_HISTORY_TEXT = By.cssSelector(".weight-empty-text");
    private static final By CLEAR_HISTORY_BUTTON = By.cssSelector(".clear-history-btn");

    @Test
    public void initialWeightShouldBe75WithEmptyHistory() {
        openHome();

        Assert.assertEquals(readWeightValue(), 75.0, 0.001, "Təmiz brauzerdə başlanğıc dəyər 75.0 olmalıdır");
        Assert.assertEquals(visible(EMPTY_HISTORY_TEXT).getText().trim(),
                "Çəkinizi qeyd etməklə tarixçə yaradın.");
        Assert.assertTrue(driver.findElements(CLEAR_HISTORY_BUTTON).isEmpty(),
                "Boş tarixçədə 'Tarixçəni təmizlə' düyməsi olmamalıdır");
    }

    @Test
    public void shouldIncreaseWeightOnceAndPersistAfterSave() {
        openHome();

        double weightBefore = readWeightValue();

        clickWhenReady(PLUS_BUTTON);

        double weightAfterClick = readWeightValue();
        Assert.assertEquals(weightAfterClick, weightBefore + 0.1, 0.001,
                "+ düyməsindən sonra dəyər 0.1 vahid artmalıdır");

        clickWhenReady(SAVE_BUTTON);

        // Yadda saxlandığını təsdiqləmək üçün səhifəni yenilə (dəyər localStorage-dan oxunur)
        refreshPage();

        double weightAfterRefresh = readWeightValue();
        Assert.assertEquals(weightAfterRefresh, weightBefore + 0.1, 0.001,
                "Refresh-dən sonra dəyər saxlanmalıdır");
    }

    @Test
    public void minusButtonShouldDecreaseWeight() {
        openHome();

        double weightBefore = readWeightValue();
        clickWhenReady(MINUS_BUTTON);

        Assert.assertEquals(readWeightValue(), weightBefore - 0.1, 0.001,
                "− düyməsindən sonra dəyər 0.1 vahid azalmalıdır");
    }

    @Test
    public void weightShouldNotGoBelowMinimum() {
        openHome();

        moveSlider(Keys.HOME);
        Assert.assertEquals(readWeightValue(), 30.0, 0.001, "Slider-in minimumu 30 kq olmalıdır");

        clickWhenReady(MINUS_BUTTON);
        Assert.assertEquals(readWeightValue(), 30.0, 0.001, "Dəyər 30 kq-dan aşağı düşməməlidir");
    }

    @Test
    public void weightShouldNotGoAboveMaximum() {
        openHome();

        moveSlider(Keys.END);
        Assert.assertEquals(readWeightValue(), 150.0, 0.001, "Slider-in maksimumu 150 kq olmalıdır");

        clickWhenReady(PLUS_BUTTON);
        Assert.assertEquals(readWeightValue(), 150.0, 0.001, "Dəyər 150 kq-dan yuxarı qalxmamalıdır");
    }

    @Test
    public void sliderShouldChangeWeight() {
        openHome();

        double weightBefore = readWeightValue();
        moveSlider(Keys.ARROW_RIGHT);
        moveSlider(Keys.ARROW_RIGHT);

        Assert.assertEquals(readWeightValue(), weightBefore + 0.2, 0.001,
                "Slider-i 2 addım sağa çəkəndə dəyər 0.2 artmalıdır");
    }

    @Test
    public void savedWeightShouldAppearInHistory() {
        openHome();

        clickWhenReady(PLUS_BUTTON);
        double expected = readWeightValue();
        clickWhenReady(SAVE_BUTTON);

        List<WebElement> items = wait.until(ExpectedConditions.numberOfElementsToBe(HISTORY_ITEMS, 1));
        Assert.assertEquals(readHistoryWeight(items.get(0)), expected, 0.001,
                "Tarixçədəki qeyd saxlanılan dəyərlə eyni olmalıdır");
        Assert.assertTrue(driver.findElements(EMPTY_HISTORY_TEXT).isEmpty(),
                "Qeyd olanda boş tarixçə mətni gizlənməlidir");
        Assert.assertNotNull(localStorageItem(HISTORY_KEY), "Tarixçə localStorage-a yazılmalıdır");
    }

    @Test
    public void historyShouldKeepOnlyLastFiveEntries() {
        openHome();

        double lastSaved = 0;
        for (int i = 0; i < 6; i++) {
            clickWhenReady(PLUS_BUTTON);
            lastSaved = readWeightValue();
            clickWhenReady(SAVE_BUTTON);
            int expectedCount = Math.min(i + 1, 5);
            wait.until(ExpectedConditions.numberOfElementsToBe(HISTORY_ITEMS, expectedCount));
        }

        List<WebElement> items = driver.findElements(HISTORY_ITEMS);
        Assert.assertEquals(items.size(), 5, "Tarixçədə maksimum 5 qeyd qalmalıdır");
        Assert.assertEquals(readHistoryWeight(items.get(0)), lastSaved, 0.001,
                "Ən yeni qeyd siyahının əvvəlində olmalıdır");
    }

    @Test
    public void clearHistoryShouldRemoveAllEntries() {
        openHome();

        clickWhenReady(SAVE_BUTTON);
        wait.until(ExpectedConditions.numberOfElementsToBe(HISTORY_ITEMS, 1));

        clickWhenReady(CLEAR_HISTORY_BUTTON);

        wait.until(ExpectedConditions.numberOfElementsToBe(HISTORY_ITEMS, 0));
        visible(EMPTY_HISTORY_TEXT);
        Assert.assertNull(localStorageItem(HISTORY_KEY), "Tarixçə localStorage-dan silinməlidir");
    }

    private void moveSlider(Keys key) {
        WebElement slider = wait.until(ExpectedConditions.presenceOfElementLocated(SLIDER));
        scrollToCenter(slider);
        slider.sendKeys(key);
    }

    private double readWeightValue() {
        WebElement el = visible(WEIGHT_VALUE);
        String text = el.getText().replace(",", ".").trim(); // vergüllü format ehtimalına qarşı
        return Double.parseDouble(text);
    }

    private double readHistoryWeight(WebElement item) {
        // Mətn "75.1 kq" formatındadır
        String text = item.findElement(HISTORY_ITEM_WEIGHT).getText().replace(",", ".").trim();
        return Double.parseDouble(text.split("\\s+")[0]);
    }
}
