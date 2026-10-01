package tests.ui;

import base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

/** "Nəfəs məşqi": hazırlıq 3 san → nəfəs al 4 san → saxla 7 san → nəfəs ver 8 san → təkrar. */
public class BreathingExerciseTest extends BaseTest {

    private static final By STATE_TEXT = By.cssSelector(".breathing-state-text");
    private static final By START_BUTTON = By.xpath(
            "//div[contains(@class,'breathing-controls')]//button[.//span[normalize-space()='Başla']]");
    private static final By STOP_BUTTON = By.xpath(
            "//div[contains(@class,'breathing-controls')]//button[.//span[normalize-space()='Dayandır']]");

    private static final String READY = "Daxili hüzur və sakitlik üçün dərin nəfəs alın";
    private static final String PREPARE = "Hazırlaşın...";
    private static final String INHALE = "Nəfəs al...";
    private static final String HOLD = "Nəfəsini saxla...";
    private static final String EXHALE = "Nəfəs ver...";

    @Test
    public void initialStateShouldShowReadyTextAndStartButton() {
        openHome();

        Assert.assertEquals(visible(STATE_TEXT).getText().trim(), READY);
        visible(START_BUTTON);
        Assert.assertTrue(driver.findElements(STOP_BUTTON).isEmpty(), "Başlamadan 'Dayandır' görünməməlidir");
    }

    @Test
    public void shouldGoThroughAllBreathingPhases() {
        openHome();
        clickWhenReady(START_BUTTON);

        Assert.assertEquals(visible(STATE_TEXT).getText().trim(), PREPARE);
        visible(STOP_BUTTON);

        // Hər mərhələnin müddəti + ehtiyat vaxt
        waitForState(INHALE, 3 + 3);
        waitForState(HOLD, 4 + 3);
        waitForState(EXHALE, 7 + 3);
        waitForState(INHALE, 8 + 3); // dövr yenidən başlayır
    }

    @Test
    public void stopButtonShouldResetToInitialState() {
        openHome();
        clickWhenReady(START_BUTTON);
        waitForState(INHALE, 3 + 3);

        clickWhenReady(STOP_BUTTON);

        wait.until(ExpectedConditions.textToBe(STATE_TEXT, READY));
        visible(START_BUTTON);
        Assert.assertTrue(driver.findElements(STOP_BUTTON).isEmpty(), "Dayandırdıqdan sonra 'Başla' qayıtmalıdır");
    }

    private void waitForState(String expected, int timeoutSeconds) {
        new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                .until(ExpectedConditions.textToBe(STATE_TEXT, expected));
    }
}
