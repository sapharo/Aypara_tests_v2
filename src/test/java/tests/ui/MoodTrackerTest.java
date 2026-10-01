package tests.ui;

import base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

/** "Əhval-ruhiyyə izləyicisi": seçim localStorage-da ("aypara_selected_mood") saxlanır. */
public class MoodTrackerTest extends BaseTest {

    private static final String MOOD_KEY = "aypara_selected_mood";

    private static final By MOOD_BUTTONS = By.cssSelector(".mood-selector-button");
    private static final By SELECTED_MOOD = By.cssSelector(".mood-selector-button.selected");
    private static final By PLACEHOLDER = By.cssSelector(".mood-placeholder");
    private static final By ADVICE_TEXT = By.cssSelector(".mood-advice-text");

    @DataProvider(name = "moods")
    public Object[][] moods() {
        // {tip, düymə mətni, tövsiyənin başlanğıcı}
        return new Object[][]{
                {"tired", "Yorğun", "Bədəninizin dincəlməyə ehtiyacı var"},
                {"stressed", "Stresli", "Dərin nəfəs alın"},
                {"neutral", "Normal", "Hər gün eyni dərəcədə hərəkətli olmaq"},
                {"calm", "Sakit", "Daxili hüzurunuzu qorumaq"},
                {"happy", "Şən", "Möhtəşəm!"},
        };
    }

    @Test
    public void shouldShowFiveMoodsAndNoSelectionByDefault() {
        openHome();

        List<WebElement> buttons = wait.until(ExpectedConditions.numberOfElementsToBe(MOOD_BUTTONS, 5));
        Assert.assertEquals(buttons.size(), 5, "5 əhval variantı olmalıdır");
        Assert.assertTrue(driver.findElements(SELECTED_MOOD).isEmpty(), "Əvvəlcədən heç nə seçilməməlidir");
        Assert.assertFalse(driver.findElements(PLACEHOLDER).isEmpty(), "Seçim olmayanda placeholder görünməlidir");
    }

    @Test(dataProvider = "moods")
    public void selectingMoodShouldHighlightItAndShowAdvice(String type, String label, String advice) {
        openHome();

        clickWhenReady(moodButton(type));

        WebElement selected = visible(SELECTED_MOOD);
        Assert.assertTrue(selected.getAttribute("class").contains("mood-" + type),
                "Seçilmiş düymə '" + type + "' olmalıdır");
        Assert.assertTrue(selected.getText().contains(label), "Düymə mətni '" + label + "' olmalıdır");
        Assert.assertTrue(visible(ADVICE_TEXT).getText().startsWith(advice),
                "'" + label + "' üçün uyğun tövsiyə göstərilməlidir");
        Assert.assertEquals(localStorageItem(MOOD_KEY), type, "Seçim localStorage-a yazılmalıdır");
    }

    @Test
    public void onlyOneMoodShouldBeSelectedAtATime() {
        openHome();

        clickWhenReady(moodButton("happy"));
        clickWhenReady(moodButton("tired"));

        wait.until(ExpectedConditions.attributeContains(moodButton("tired"), "class", "selected"));
        List<WebElement> selected = driver.findElements(SELECTED_MOOD);
        Assert.assertEquals(selected.size(), 1, "Eyni anda yalnız bir əhval seçilə bilər");
        Assert.assertTrue(selected.get(0).getAttribute("class").contains("mood-tired"));
    }

    @Test
    public void selectedMoodShouldPersistAfterRefresh() {
        openHome();
        clickWhenReady(moodButton("calm"));
        visible(SELECTED_MOOD);

        refreshPage();

        WebElement selected = visible(SELECTED_MOOD);
        Assert.assertTrue(selected.getAttribute("class").contains("mood-calm"),
                "Refresh-dən sonra seçim saxlanmalıdır");
    }

    private By moodButton(String type) {
        return By.cssSelector(".mood-selector-button.mood-" + type);
    }
}
