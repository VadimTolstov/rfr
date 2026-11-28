package io.student.rangiffler.config;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import io.student.rangiffler.jupiter.extension.BrowserOptions;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Verifies documented browser dimensions at the options and browser boundaries. */
@Isolated
@UsePlaywright(BrowserOptions.class)
final class BrowserDimensionsTest {
    @ParameterizedTest
    @CsvSource({"1920, 1080", "390, 844"})
    @DisplayName("Документированные ключи задают размер экрана и области просмотра")
    void appliesDocumentedDimensions(int width, int height) {
        List<String> keys = List.of("rfr.screenWidth", "rfr.screenHeight", "rfr.viewportWidth", "rfr.viewportHeight");
        List<String> previous = keys.stream().map(System::getProperty).toList();
        try {
            System.setProperty(keys.get(0), Integer.toString(width));
            System.setProperty(keys.get(1), Integer.toString(height));
            System.setProperty(keys.get(2), "800");
            System.setProperty(keys.get(3), "600");
            var options = new BrowserOptions().getOptions().contextOptions;
            var viewport = options.viewportSize.orElseThrow();
            assertAll(
                    () -> assertEquals(width, viewport.width),
                    () -> assertEquals(height, viewport.height),
                    () -> assertEquals(width, options.screenSize.width),
                    () -> assertEquals(height, options.screenSize.height)
            );
        } finally {
            for (int index = 0; index < keys.size(); index++) {
                String value = previous.get(index);
                if (value == null) {
                    System.clearProperty(keys.get(index));
                } else {
                    System.setProperty(keys.get(index), value);
                }
            }
        }
    }

    @Test
    @Tag("ui")
    @DisplayName("Браузер применяет конфигурацию к экрану и области просмотра")
    void exposesConfiguredDimensions(Page page, TestReporter reporter) {
        int width = TestConfig.screenWidth();
        int height = TestConfig.screenHeight();
        var viewport = page.viewportSize();
        String screen = String.valueOf(page.evaluate("() => screen.width + 'x' + screen.height"));
        assertAll(
                () -> assertEquals(width, viewport.width),
                () -> assertEquals(height, viewport.height),
                () -> assertEquals(width + "x" + height, screen)
        );
        reporter.publishEntry("browser.screen", screen);
        reporter.publishEntry("browser.viewport", viewport.width + "x" + viewport.height);
    }
}
