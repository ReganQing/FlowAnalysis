package desktop.view.component;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HtmlPreviewViewTest {

    @TempDir
    Path tempDir;

    @BeforeAll
    static void startJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
        }
    }

    @Test
    void fallsBackWhenWebViewNativeLibraryCannotBeLoaded() throws Exception {
        Path report = tempDir.resolve("report.html");
        Files.writeString(report, "<html><body>report</body></html>");

        HtmlPreviewView view = onFxThread(() ->
            new HtmlPreviewView(() -> {
                throw new NoClassDefFoundError("Could not initialize class com.sun.webkit.WebPage");
            }));

        onFxThread(() -> {
            assertDoesNotThrow(() -> view.loadReport(report.toString()));
            assertFalse(view.isWebViewAvailable());
            assertTrue(view.getCenter().getStyleClass().contains("html-preview-fallback"));
            return null;
        });
    }

    private static <T> T onFxThread(ThrowingSupplier<T> supplier) throws Exception {
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                result.set(supplier.get());
            } catch (Throwable throwable) {
                error.set(throwable);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError(error.get());
        }
        return result.get();
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
