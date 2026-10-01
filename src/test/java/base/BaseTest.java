package base;

import io.qameta.allure.testng.AllureTestNg;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

import java.time.Duration;

@Listeners(AllureTestNg.class)
public class BaseTest {

    protected static final String BASE_URL = "https://aypara.app";
    protected static final String HOME_URL = BASE_URL + "/az";

    private static final By SPLASH_SCREEN = By.cssSelector(".splash-screen");
    private static final By COOKIE_DECLINE_BUTTON = By.cssSelector(".cookie-consent-box .cookie-decline");

    protected WebDriver driver;
    protected WebDriverWait wait;
    private boolean cookiesHandled;

    @BeforeMethod
    public void setUp() {
        cookiesHandled = false;
        driver = new ChromeDriver(createOptions());
        // Headless rejimdə maximize() --window-size-ı ləğv edib pəncərəni kiçildir (784x441),
        // ona görə yalnız lokal (görünən) brauzerdə çağırırıq
        if (!isCi()) {
            driver.manage().window().maximize();
        }
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /** Alt klasslar (məs. mobil testlər) brauzer parametrlərini dəyişmək üçün override edə bilər. */
    protected ChromeOptions createOptions() {
        ChromeOptions options = new ChromeOptions();

        if (isCi()) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
        }
        return options;
    }

    // GitHub Actions kimi server mühitlərində headless lazımdır.
    // Lokal işlədəndə normal (görünən) brauzer açılsın deyə, environment variable ilə idarə edirik.
    protected static boolean isCi() {
        return System.getenv("CI") != null;
    }

    /** Səhifəni açır, splash ekranının yox olmasını gözləyir və cookie bannerini bağlayır. */
    protected void open(String url) {
        driver.get(url);
        waitForSplashToDisappear();
        declineCookiesIfAsked();
    }

    /**
     * İlk girişdə cookie banneri çıxır və düymələrin üstünü örtür. "İmtina edirəm" seçirik ki,
     * testlər Google Analytics-i yükləməsin və statistikanı korlamasın.
     * Seçim localStorage-da saxlanır, ona görə hər brauzer sessiyasında yalnız bir dəfə yoxlanır.
     */
    private void declineCookiesIfAsked() {
        if (cookiesHandled) {
            return;
        }
        try {
            new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.elementToBeClickable(COOKIE_DECLINE_BUTTON))
                    .click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(COOKIE_DECLINE_BUTTON));
        } catch (TimeoutException ignored) {
            // Banner çıxmadı (məs. 404 səhifəsi) — davam edirik
        }
        cookiesHandled = true;
    }

    protected void openHome() {
        open(HOME_URL);
    }

    protected void refreshPage() {
        driver.navigate().refresh();
        waitForSplashToDisappear();
    }

    protected void waitForSplashToDisappear() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(SPLASH_SCREEN));
    }

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void clickWhenReady(By locator) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        scrollToCenter(element);
        element.click();
    }

    protected void scrollToCenter(WebElement element) {
        js("arguments[0].scrollIntoView({block:'center'});", element);
    }

    protected Object js(String script, Object... args) {
        return ((JavascriptExecutor) driver).executeScript(script, args);
    }

    protected String localStorageItem(String key) {
        return (String) js("return window.localStorage.getItem(arguments[0]);", key);
    }

    /** Header linkləri bölməni ekranın yuxarısından 80px aşağıya (header-in altına) scroll edir. */
    protected boolean isScrolledBelowHeader(WebElement element) {
        return (Boolean) js(
                "return Math.abs(arguments[0].getBoundingClientRect().top - 80) < 5;", element);
    }

    protected boolean isInViewport(WebElement element) {
        return (Boolean) js(
                "const r = arguments[0].getBoundingClientRect();"
                        + "return r.top < window.innerHeight && r.bottom > 0;", element);
    }
}
