package desktop.view.component;

import desktop.model.ChatMessage;
import desktop.model.MessageRole;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 消息气泡组件 — 显示单条用户或 AI 消息。
 * <p>
 * AI 消息优先使用 WebView 渲染 Markdown HTML；
 * 若 jfxwebkit 原生库不可用（例如 mvn exec:java 未配置 library path），
 * 自动降级为 MarkdownTextFlow（TextFlow 渲染）。
 * <p>
 * 流式响应阶段使用 MarkdownTextFlow 实时渲染 Markdown，
 * 完成后切换为 WebView（如可用）以获得最佳渲染效果。
 */
public class MessageBubble extends VBox {

    private static final MarkdownRenderer MARKDOWN = new MarkdownRenderer();
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final double MAX_WIDTH = 680;

    /** 缓存 WebView 是否可用，避免每次创建都触发 UnsatisfiedLinkError */
    private static final boolean WEB_VIEW_AVAILABLE = checkWebViewAvailable();

    private WebView markdownView;
    private MarkdownTextFlow streamingView;
    private boolean useWebView;

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

    /**
     * 私有构造器，用于创建流式占位气泡（不创建 WebView）。
     */
    private MessageBubble(@SuppressWarnings("unused") Void streaming) {
        setMaxWidth(MAX_WIDTH);
        setPadding(new Insets(4, 0, 0, 0));
        setSpacing(4);
    }

    // ── 用户消息 ──────────────────────────────────────────────

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

    // ── AI 消息（历史记录加载） ─────────────────────────────────

    private void buildAiBubble(ChatMessage message) {
        setAlignment(Pos.CENTER_LEFT);

        HBox header = buildAiHeader(message.modelName());
        useWebView = WEB_VIEW_AVAILABLE;

        if (useWebView) {
            markdownView = createWebView(message.content());
            getChildren().addAll(header, markdownView);
        } else {
            MarkdownTextFlow flow = createMarkdownFlow(message.content());
            getChildren().addAll(header, flow);
        }

        Label meta = new Label(message.formattedTime());
        meta.getStyleClass().add("message-meta");
        getChildren().add(meta);
    }

    // ── 流式占位气泡 ──────────────────────────────────────────

    /**
     * 创建流式响应的占位气泡。使用 MarkdownTextFlow 实时渲染 Markdown。
     */
    public static MessageBubble createStreamingPlaceholder(String modelName) {
        MessageBubble bubble = new MessageBubble((Void) null);
        bubble.setAlignment(Pos.CENTER_LEFT);

        HBox header = buildAiHeader(modelName);

        bubble.streamingView = new MarkdownTextFlow();
        bubble.streamingView.setMaxWidth(MAX_WIDTH);
        bubble.streamingView.setStyle("-fx-padding: 0;");

        bubble.getChildren().addAll(header, bubble.streamingView);
        return bubble;
    }

    // ── 内容更新 ──────────────────────────────────────────────

    public void updateContent(String markdownContent) {
        if (streamingView != null) {
            streamingView.updateMarkdown(markdownContent);
        } else if (markdownView != null) {
            markdownView.getEngine().loadContent(
                    MARKDOWN.renderToHtml(markdownContent));
        }
    }

    /**
     * 流式响应完成后，将 MarkdownTextFlow 替换为最终渲染组件（WebView 或 MarkdownTextFlow），
     * 并追加时间戳 meta 标签。
     */
    public void completeContent(String markdownContent) {
        if (streamingView == null) {
            // 非流式场景，直接更新已有组件
            updateContent(markdownContent);
            return;
        }

        int index = getChildren().indexOf(streamingView);
        if (index < 0) {
            return;
        }

        // 强制刷新节流期间缓存的内容
        streamingView.forceRender();

        useWebView = WEB_VIEW_AVAILABLE;

        if (useWebView) {
            markdownView = createWebView(markdownContent);
            getChildren().set(index, markdownView);
        } else {
            // WebView 不可用时，保留当前 MarkdownTextFlow（已 forceRender）
            getChildren().set(index, streamingView);
        }
        streamingView = null;

        // 追加时间戳（与 buildAiBubble 保持一致）
        Label meta = new Label(LocalDateTime.now().format(TIME_FMT));
        meta.getStyleClass().add("message-meta");
        getChildren().add(meta);
    }

    // ── 内部工具方法 ──────────────────────────────────────────

    private static HBox buildAiHeader(String modelName) {
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        Label sender = new Label("AI  ·  " + modelName);
        sender.getStyleClass().add("message-sender");
        header.getChildren().add(sender);
        return header;
    }

    private WebView createWebView(String markdownContent) {
        WebView view = new WebView();
        view.getStyleClass().addAll("message-bubble", "message-bubble-ai");
        view.setMaxWidth(MAX_WIDTH);
        view.setPrefHeight(240);
        view.getEngine().setJavaScriptEnabled(false);
        view.getEngine().loadContent(MARKDOWN.renderToHtml(markdownContent));
        return view;
    }

    private static MarkdownTextFlow createMarkdownFlow(String markdownContent) {
        MarkdownTextFlow flow = new MarkdownTextFlow();
        flow.setMaxWidth(MAX_WIDTH);
        flow.setStyle("-fx-padding: 0;");
        flow.updateMarkdown(markdownContent);
        return flow;
    }

    /**
     * 检测 WebView（jfxwebkit 原生库）是否可用。
     * 仅在类加载时执行一次，结果缓存到 {@link #WEB_VIEW_AVAILABLE}。
     * <p>
     * 直接尝试 new WebView() 而非 System.loadLibrary，
     * 更准确地反映被守护的操作，避免重复加载的 UnsatisfiedLinkError。
     */
    private static boolean checkWebViewAvailable() {
        try {
            new WebView();
            return true;
        } catch (UnsatisfiedLinkError | NoClassDefFoundError | IllegalStateException e) {
            return false;
        }
    }
}
