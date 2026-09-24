package tests;

import base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.Test;

public class QuizTest extends BaseTest {

    private static final String URL = "https://aypara.app/az";

    private static final By SPLASH_SCREEN = By.cssSelector(".splash-screen");

    private static final By START_BUTTON = By.xpath(
            "//button[contains(@class,'premium-btn-primary')][.//span[normalize-space()='Testə başla']]");

    private static final By QUESTION_1_OPTION = By.xpath(
            "//button[contains(@class,'quiz-option-btn')][contains(normalize-space(.), '2 litrdən çox')]");

    private static final By QUESTION_2_OPTION = By.xpath(
            "//button[contains(@class,'quiz-option-btn')][contains(normalize-space(.), 'Bəzən planlayıram, bəzən çatdırmıram')]");

    private static final By QUESTION_3_OPTION = By.xpath(
            "//button[contains(@class,'quiz-option-btn')][contains(normalize-space(.), 'Hərdən gərgin və yorğun oluram')]");

    @Test
    public void shouldCompleteFirstThreeQuizQuestions() {
        driver.get(URL);
        waitForSplashToDisappear();

        clickWhenReady(START_BUTTON);
        clickWhenReady(QUESTION_1_OPTION);
        clickWhenReady(QUESTION_2_OPTION);
        clickWhenReady(QUESTION_3_OPTION);
    }

    private void clickWhenReady(By locator) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        scrollToCenter(element);
        element.click();
    }

    private void waitForSplashToDisappear() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(SPLASH_SCREEN));
    }

    private void scrollToCenter(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", element);
    }
}