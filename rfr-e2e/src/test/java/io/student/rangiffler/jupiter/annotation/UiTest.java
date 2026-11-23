package io.student.rangiffler.jupiter.annotation;

import com.microsoft.playwright.junit.UsePlaywright;
import io.student.rangiffler.jupiter.extension.BrowserOptions;
import io.student.rangiffler.jupiter.extension.BrowserTimeoutExtension;
import io.student.rangiffler.jupiter.extension.BrowserTraceExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

/**
 * Identifies browser tests and supplies their shared Playwright configuration.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Tag("ui")
@UsePlaywright(BrowserOptions.class)
@ExtendWith({
        BrowserTimeoutExtension.class,
        BrowserTraceExtension.class,
})
public @interface UiTest {
}
