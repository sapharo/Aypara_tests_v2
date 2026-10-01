package tests.ui;

import base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Header-dəki "Əlaqə" düyməsi ilə açılan form.
 *
 * Form real /api/support ünvanına POST göndərir. Nightly-də real müraciət yaranmasın deyə,
 * hər testdə window.fetch-i həmin ünvan üçün saxta cavabla əvəz edirik və göndərilən sorğuları
 * window.__supportRequests massivində toplayırıq.
 */
public class ContactFormTest extends BaseTest {

    private static final By CONTACT_BUTTON = By.cssSelector("header button[aria-label='Contact Us']");
    private static final By MODAL = By.cssSelector(".modal-content");
    private static final By MODAL_OVERLAY = By.cssSelector(".modal-overlay");
    private static final By MODAL_TITLE = By.cssSelector(".modal-content h2");
    private static final By CLOSE_BUTTON = By.xpath("//div[contains(@class,'modal-content')]//button[normalize-space()='×']");
    private static final By NAME_INPUT = By.id("name");
    private static final By EMAIL_INPUT = By.id("email");
    private static final By PHONE_INPUT = By.id("phone");
    private static final By MESSAGE_INPUT = By.id("message");
    private static final By SUBMIT_BUTTON = By.cssSelector(".modal-content button[type='submit']");
    private static final By SUPPORT_EMAIL_LINK = By.cssSelector(".modal-content a[href='mailto:support@aypara.app']");

    private static final String SUCCESS_TEXT = "Mesajınız uğurla göndərildi! Təşəkkür edirik.";

    @Test
    public void contactButtonShouldOpenModal() {
        openContactForm();

        Assert.assertEquals(visible(MODAL_TITLE).getText().trim(), "Bizimlə əlaqə");
        Assert.assertEquals(visible(SUPPORT_EMAIL_LINK).getText().trim(), "support@aypara.app");
    }

    @Test
    public void closeButtonShouldCloseModal() {
        openContactForm();

        clickWhenReady(CLOSE_BUTTON);

        wait.until(ExpectedConditions.invisibilityOfElementLocated(MODAL_OVERLAY));
    }

    @Test
    public void emptyFormShouldNotBeSubmitted() {
        openContactForm();

        clickWhenReady(SUBMIT_BUTTON);

        Assert.assertFalse(isValid(NAME_INPUT), "Ad məcburi sahədir");
        Assert.assertFalse(isValid(MESSAGE_INPUT), "Mesaj məcburi sahədir");
        assertNoRequestSent();
        Assert.assertTrue(driver.findElement(MODAL).isDisplayed(), "Modal açıq qalmalıdır");
    }

    @Test
    public void formWithoutMessageShouldNotBeSubmitted() {
        openContactForm();

        type(NAME_INPUT, "Test İstifadəçi");
        clickWhenReady(SUBMIT_BUTTON);

        Assert.assertTrue(isValid(NAME_INPUT));
        Assert.assertFalse(isValid(MESSAGE_INPUT), "Mesaj boş olanda form göndərilməməlidir");
        assertNoRequestSent();
    }

    @Test
    public void invalidEmailShouldBeRejected() {
        openContactForm();

        fillRequiredFields();
        type(EMAIL_INPUT, "abc@");
        clickWhenReady(SUBMIT_BUTTON);

        Assert.assertFalse(isValid(EMAIL_INPUT), "'abc@' düzgün e-poçt sayılmamalıdır");
        assertNoRequestSent();
    }

    @Test
    public void phoneFieldShouldIgnoreLetters() {
        openContactForm();

        type(PHONE_INPUT, "abc");

        Assert.assertEquals(driver.findElement(PHONE_INPUT).getAttribute("value"), "",
                "Telefon sahəsi hərfləri qəbul etməməlidir");
    }

    @Test
    public void invalidPhoneShouldBeRejected() {
        openContactForm();

        fillRequiredFields();
        type(PHONE_INPUT, "12345");
        clickWhenReady(SUBMIT_BUTTON);

        Assert.assertFalse(isValid(PHONE_INPUT), "'12345' düzgün telefon nömrəsi sayılmamalıdır");
        assertNoRequestSent();
    }

    @Test
    public void validFormShouldBeSubmittedAndShowSuccess() {
        openContactForm();

        fillRequiredFields();
        type(EMAIL_INPUT, "test@example.com");
        type(PHONE_INPUT, "+994501234567");
        clickWhenReady(SUBMIT_BUTTON);

        wait.until(ExpectedConditions.textToBePresentInElementLocated(MODAL, SUCCESS_TEXT));

        List<?> requests = sentRequests();
        Assert.assertEquals(requests.size(), 1, "Yalnız bir sorğu göndərilməlidir");
        String body = String.valueOf(requests.get(0));
        Assert.assertTrue(body.contains("\"name\":\"Test İstifadəçi\""), "Sorğuda ad olmalıdır: " + body);
        Assert.assertTrue(body.contains("\"email\":\"test@example.com\""), "Sorğuda e-poçt olmalıdır: " + body);
        Assert.assertTrue(body.contains("\"message\":\"Avtomatlaşdırılmış test mesajı\""),
                "Sorğuda mesaj olmalıdır: " + body);
    }

    @Test
    public void serverErrorShouldShowErrorMessage() {
        openContactForm();
        js("window.__supportStatus = 500;");

        fillRequiredFields();
        clickWhenReady(SUBMIT_BUTTON);

        wait.until(ExpectedConditions.textToBePresentInElementLocated(MODAL,
                "Xəta baş verdi. Zəhmət olmasa yenidən cəhd edin."));
        Assert.assertFalse(driver.findElement(MODAL).getText().contains(SUCCESS_TEXT));
    }

    private void openContactForm() {
        openHome();
        stubSupportApi();
        clickWhenReady(CONTACT_BUTTON);
        visible(MODAL);
    }

    private void stubSupportApi() {
        js("window.__supportRequests = [];"
                + "window.__supportStatus = 200;"
                + "const originalFetch = window.fetch;"
                + "window.fetch = function (url, options) {"
                + "  if (String(url).includes('/api/support')) {"
                + "    window.__supportRequests.push(options && options.body);"
                + "    return Promise.resolve(new Response('{}', {status: window.__supportStatus}));"
                + "  }"
                + "  return originalFetch.apply(this, arguments);"
                + "};");
    }

    private void fillRequiredFields() {
        type(NAME_INPUT, "Test İstifadəçi");
        type(MESSAGE_INPUT, "Avtomatlaşdırılmış test mesajı");
    }

    private void type(By locator, String text) {
        WebElement input = visible(locator);
        input.clear();
        input.sendKeys(text);
    }

    private boolean isValid(By locator) {
        return (Boolean) js("return arguments[0].checkValidity();", driver.findElement(locator));
    }

    private List<?> sentRequests() {
        return (List<?>) js("return window.__supportRequests;");
    }

    private void assertNoRequestSent() {
        Assert.assertTrue(sentRequests().isEmpty(), "Səhv formda sorğu göndərilməməlidir");
    }
}
