package desktop.view.component;

import desktop.model.ChatMessage;
import desktop.model.MessageRole;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

/**
 * 消息气泡组件 — 显示单条用户或 AI 消息。
 */
public class MessageBubble extends VBox {

    private static final MarkdownRenderer MARKDOWN = new MarkdownRenderer();
    private static final double MAX_WIDTH = 680;

    public MessageBubble(ChatMessage message) {
        setMaxWidth(MAX_WIDTH);
        setPadding(new Insets(4, 0, 0, 0));
        setSpacing(4);

        if (message.role() == MessageRole.USER) {
            buildUserBubble(message);
        } else if (message.role() == MessageRole.AI) {
            buildAiBubble(message);
        }
    }

    private void buildUserBubble(ChatMessage message) {
        setAlignment(Pos.CENTER_RIGHT);

        VBox bubble = new VBox();
        bubble.getStyleClass().addAll("message-bubble", "message-bubble-user");
        bubble.setMaxWidth(MAX_WIDTH);

        Label text = new Label(message.content());
        text.getStyleClass().add("message-text");
        text.setWrapText(true);
        text.setMaxWidth(MAX_WIDTH - 32);

        Label meta = new Label(message.formattedTime());
        meta.getStyleClass().add("message-meta");
        meta.setAlignment(Pos.CENTER_RIGHT);

        bubble.getChildren().addAll(text, meta);
        getChildren().add(bubble);
    }

    private void buildAiBubble(ChatMessage message) {
        setAlignment(Pos.CENTER_LEFT);

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        Label sender = new Label("AI  ·  " + message.modelName());
        sender.getStyleClass().add("message-sender");
        header.getChildren().add(sender);

        WebView webView = new WebView();
        webView.getStyleClass().addAll("message-bubble", "message-bubble-ai");
        webView.setMaxWidth(MAX_WIDTH);
        webView.setPrefHeight(-1);
        webView.getEngine().loadContent(MARKDOWN.renderToHtml(message.content()));
        webView.getEngine().setJavaScriptEnabled(false);

        Label meta = new Label(message.formattedTime());
        meta.getStyleClass().add("message-meta");

        getChildren().addAll(header, webView, meta);
    }

    public static MessageBubble createStreamingPlaceholder(String modelName) {
        return new MessageBubble(new ChatMessage(
            null, null, MessageRole.AI, "", modelName,
            null, null, null, java.time.LocalDateTime.now()
        ));
    }

    public void updateContent(String markdownContent) {
        getChildren().stream()
            .filter(node -> node instanceof WebView)
            .map(node -> (WebView) node)
            .findFirst()
            .ifPresent(wv -> wv.getEngine().loadContent(
                MARKDOWN.renderToHtml(markdownContent)));
    }
}
