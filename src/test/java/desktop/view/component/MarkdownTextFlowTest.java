package desktop.view.component;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarkdownTextFlowTest {

    @BeforeAll
    static void startJavaFx() throws Exception {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
        }
    }

    @Test
    void rendersTextAfterHardBreakInANewTextFlow() throws Exception {
        MarkdownTextFlow view = render("🌙 **标题**  \n书名本身就是一个绝妙的隐喻");

        assertEquals(2, view.getChildren().size());
        TextFlow secondLine = (TextFlow) view.getChildren().get(1);
        assertEquals("书名本身就是一个绝妙的隐喻", textContent(secondLine));
    }

    @Test
    void rendersEmojiWithEmojiFontInSeparateTextNode() throws Exception {
        MarkdownTextFlow view = render("🌙 标题");
        TextFlow line = (TextFlow) view.getChildren().get(0);
        List<Text> textNodes = line.getChildren().stream()
            .filter(Text.class::isInstance)
            .map(Text.class::cast)
            .toList();

        assertEquals("🌙", textNodes.get(0).getText());
        assertTrue(textNodes.get(0).getStyle().contains("Segoe UI Emoji"));
        assertEquals(" 标题", textNodes.get(1).getText());
    }

    private MarkdownTextFlow render(String markdown) throws Exception {
        AtomicReference<MarkdownTextFlow> result = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            MarkdownTextFlow view = new MarkdownTextFlow();
            view.updateMarkdown(markdown);
            view.forceRender();
            result.set(view);
            latch.countDown();
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        return result.get();
    }

    private String textContent(TextFlow flow) {
        return flow.getChildren().stream()
            .filter(Text.class::isInstance)
            .map(Text.class::cast)
            .map(Text::getText)
            .reduce("", String::concat);
    }
}
