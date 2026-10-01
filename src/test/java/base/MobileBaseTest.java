package base;

import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Map;

/**
 * Chrome-un mobil emulyasiyası ilə işləyən testlər üçün baza.
 * Alt klass hansı cihazın user-agent-ini istifadə edəcəyini seçir.
 */
public abstract class MobileBaseTest extends BaseTest {

    protected static final String IPHONE_USER_AGENT =
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 "
                    + "(KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1";

    protected static final String ANDROID_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/129.0.0.0 Mobile Safari/537.36";

    protected abstract String userAgent();

    @Override
    protected ChromeOptions createOptions() {
        ChromeOptions options = super.createOptions();
        options.setExperimentalOption("mobileEmulation", Map.of(
                "deviceMetrics", Map.of("width", 390, "height", 844, "pixelRatio", 3.0, "touch", true),
                "userAgent", userAgent()));
        return options;
    }
}
