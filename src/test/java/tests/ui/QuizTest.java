package tests.ui;

import base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Həyat balansı testi: 3 sual, hər sualda A/B/C (1/2/3 bal).
 * Skor = round((q1 + q2 + q3) / 9 * 100); < 50 aşağı, 50–80 orta, > 80 yüksək.
 */
public class QuizTest extends BaseTest {

    private static final By START_BUTTON = By.xpath(
            "//button[contains(@class,'premium-btn-primary')][.//span[normalize-space()='Testə başla']]");
    private static final By PROGRESS_LABEL = By.cssSelector(".quiz-progress-label");
    private static final By QUESTION_TITLE = By.cssSelector(".quiz-question-title");
    private static final By OPTION_BUTTONS = By.cssSelector(".quiz-option-btn");
    private static final By RESULT_TITLE = By.cssSelector(".quiz-result-title");
    private static final By SCORE = By.cssSelector(".quiz-score-num");
    private static final By RESULT_DESCRIPTION = By.cssSelector(".quiz-result-description");
    private static final By CTA_LINK = By.cssSelector(".quiz-btn-row a");
    private static final By RETRY_BUTTON = By.xpath(
            "//div[contains(@class,'quiz-btn-row')]//button[.//span[normalize-space()='Yenidən yoxla']]");

    private static final By QUESTION_1_OPTION = By.xpath(
            "//button[contains(@class,'quiz-option-btn')][contains(normalize-space(.), '2 litrdən çox')]");
    private static final By QUESTION_2_OPTION = By.xpath(
            "//button[contains(@class,'quiz-option-btn')][contains(normalize-space(.), 'Bəzən planlayıram, bəzən çatdırmıram')]");
    private static final By QUESTION_3_OPTION = By.xpath(
            "//button[contains(@class,'quiz-option-btn')][contains(normalize-space(.), 'Hərdən gərgin və yorğun oluram')]");

    private static final String LOW_DESCRIPTION = "Həyat balansınız olduqca aşağıdır";
    private static final String MID_DESCRIPTION = "Həyat balansınız yaxşıdır, lakin";
    private static final String HIGH_DESCRIPTION = "Təbriklər! Həyat balansınız yüksək";

    @Test
    public void shouldCompleteQuizAndShowMidScore() {
        openHome();

        clickWhenReady(START_BUTTON);
        clickWhenReady(QUESTION_1_OPTION); // C = 3
        clickWhenReady(QUESTION_2_OPTION); // B = 2
        clickWhenReady(QUESTION_3_OPTION); // B = 2

        visible(RESULT_TITLE);
        Assert.assertEquals(readScore(), 78, "3 + 2 + 2 = 7 → 7/9 = 78%");
        Assert.assertTrue(visible(RESULT_DESCRIPTION).getText().contains(MID_DESCRIPTION),
                "78% üçün 'orta' təsvir göstərilməlidir");
    }

    @DataProvider(name = "answers")
    public Object[][] answers() {
        // {cavablar (1=A, 2=B, 3=C), gözlənilən skor, gözlənilən təsvir}
        return new Object[][]{
                {new int[]{1, 1, 1}, 33, LOW_DESCRIPTION},
                {new int[]{1, 1, 2}, 44, LOW_DESCRIPTION},  // aşağı kateqoriyanın sərhədi
                {new int[]{1, 2, 2}, 56, MID_DESCRIPTION},  // orta kateqoriyanın başlanğıcı
                {new int[]{2, 2, 3}, 78, MID_DESCRIPTION},  // orta kateqoriyanın sonu
                {new int[]{2, 3, 3}, 89, HIGH_DESCRIPTION}, // yüksək kateqoriyanın başlanğıcı
                {new int[]{3, 3, 3}, 100, HIGH_DESCRIPTION},
        };
    }

    @Test(dataProvider = "answers")
    public void scoreAndDescriptionShouldMatchAnswers(int[] answers, int expectedScore, String expectedDescription) {
        openHome();
        clickWhenReady(START_BUTTON);

        for (int i = 0; i < answers.length; i++) {
            answerQuestion(i + 1, answers[i]);
        }

        Assert.assertEquals(readScore(), expectedScore, "Skor düzgün hesablanmalıdır");
        Assert.assertTrue(visible(RESULT_DESCRIPTION).getText().contains(expectedDescription),
                "Skor " + expectedScore + " üçün təsvir '" + expectedDescription + "' ilə başlamalıdır");
    }

    @Test
    public void progressLabelShouldAdvanceWithEachQuestion() {
        openHome();
        clickWhenReady(START_BUTTON);

        for (int question = 1; question <= 3; question++) {
            String expected = question + " / 3";
            wait.until(ExpectedConditions.textToBe(PROGRESS_LABEL, expected));
            Assert.assertFalse(visible(QUESTION_TITLE).getText().isBlank(), "Sual mətni boş olmamalıdır");
            Assert.assertEquals(driver.findElements(OPTION_BUTTONS).size(), 3, "Hər sualda 3 variant olmalıdır");
            clickOption(1);
        }

        visible(RESULT_TITLE);
        Assert.assertTrue(driver.findElements(PROGRESS_LABEL).isEmpty(),
                "Nəticə ekranında progress etiketi olmamalıdır");
    }

    @Test
    public void retryButtonShouldReturnToStartScreen() {
        openHome();
        clickWhenReady(START_BUTTON);
        for (int question = 1; question <= 3; question++) {
            answerQuestion(question, 2);
        }
        visible(RESULT_TITLE);

        clickWhenReady(RETRY_BUTTON);

        visible(START_BUTTON);
        Assert.assertTrue(driver.findElements(RESULT_TITLE).isEmpty(), "Nəticə ekranı bağlanmalıdır");

        // Yenidən başlayanda 1-ci sualdan başlamalıdır
        clickWhenReady(START_BUTTON);
        wait.until(ExpectedConditions.textToBe(PROGRESS_LABEL, "1 / 3"));
    }

    @Test
    public void ctaButtonShouldLeadToDownloadSection() {
        openHome();
        clickWhenReady(START_BUTTON);
        for (int question = 1; question <= 3; question++) {
            answerQuestion(question, 3);
        }

        WebElement cta = visible(CTA_LINK);
        Assert.assertEquals(cta.getText().trim(), "Tətbiqi yüklə");
        Assert.assertTrue(cta.getAttribute("href").endsWith("#download"),
                "CTA yükləmə bölməsinə aparmalıdır, href: " + cta.getAttribute("href"));

        scrollToCenter(cta);
        cta.click();

        wait.until(ExpectedConditions.urlContains("#download"));
        WebElement downloadSection = driver.findElement(By.id("download"));
        wait.until(d -> isInViewport(downloadSection));
    }

    private void answerQuestion(int questionNumber, int option) {
        wait.until(ExpectedConditions.textToBe(PROGRESS_LABEL, questionNumber + " / 3"));
        clickOption(option);
    }

    /** @param option 1-dən başlayan variant nömrəsi (1 = A, 2 = B, 3 = C) */
    private void clickOption(int option) {
        List<WebElement> options = wait.until(
                ExpectedConditions.numberOfElementsToBe(OPTION_BUTTONS, 3));
        WebElement button = options.get(option - 1);
        scrollToCenter(button);
        button.click();
    }

    private int readScore() {
        // Mətn "78%" formatındadır
        String text = visible(SCORE).getText().replace("%", "").trim();
        return Integer.parseInt(text);
    }
}
