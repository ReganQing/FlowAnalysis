package desktop.view.component;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

/**
 * HTML 报告预览视图。优先使用 WebView 渲染，jfxwebkit 不可用时降级为浏览器打开入口。
 */
public class HtmlPreviewView extends BorderPane {

    private final WebView webView;
    private final Label fallbackPath;
    private final Label fallbackError;
    private File reportFile;

    public HtmlPreviewView() {
        this(WebView::new);
    }

    HtmlPreviewView(Supplier<WebView> webViewFactory) {
        getStyleClass().add("preview-content");

        WebView createdWebView = null;
        Label createdFallbackPath = null;
        Label createdFallbackError = null;
        try {
            createdWebView = webViewFactory.get();
            createdWebView.getEngine().setJavaScriptEnabled(false);
            createdWebView.setPrefHeight(Double.MAX_VALUE);
            setCenter(createdWebView);
        } catch (LinkageError | IllegalStateException e) {
            createdFallbackPath = new Label();
            createdFallbackPath.setWrapText(true);
            createdFallbackPath.setStyle("-fx-text-fill: #6B7685;");

            createdFallbackError = new Label();
            createdFallbackError.setWrapText(true);
            createdFallbackError.setStyle("-fx-text-fill: #F87171;");

            Label message = new Label("当前运行环境无法内嵌预览 HTML 报告。");
            message.setWrapText(true);
            Button openButton = new Button("使用默认浏览器打开");
            openButton.setOnAction(event -> openInBrowser());

            VBox fallback = new VBox(12, message, createdFallbackPath, openButton, createdFallbackError);
            fallback.getStyleClass().add("html-preview-fallback");
            fallback.setAlignment(Pos.CENTER);
            fallback.setStyle("-fx-padding: 24;");
            setCenter(fallback);
        }

        webView = createdWebView;
        fallbackPath = createdFallbackPath;
        fallbackError = createdFallbackError;
    }

    /** 加载 HTML 报告文件。 */
    public void loadReport(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            reportFile = file.getAbsoluteFile();
            if (webView != null) {
                webView.getEngine().load(reportFile.toURI().toString());
            } else {
                fallbackPath.setText(reportFile.getAbsolutePath());
            }
        }
    }

    public boolean isWebViewAvailable() {
        return webView != null;
    }

    /** 获取内部 WebView；降级模式下返回 null。 */
    public WebView getWebView() {
        return webView;
    }

    private void openInBrowser() {
        if (reportFile == null) {
            return;
        }
        try {
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                fallbackError.setText("当前系统不支持自动打开浏览器。");
                return;
            }
            Desktop.getDesktop().browse(reportFile.toURI());
            fallbackError.setText("");
        } catch (IOException | SecurityException e) {
            fallbackError.setText("打开报告失败: " + e.getMessage());
        }
    }
}
