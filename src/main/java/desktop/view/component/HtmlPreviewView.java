package desktop.view.component;

import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;

import java.io.File;

/**
 * HTML 报告预览视图 — 包裹 WebView 渲染分析报告。
 * <p>
 * 注意：WebView 是 final 类，无法继承，因此使用组合模式。
 */
public class HtmlPreviewView extends BorderPane {

    private final WebView webView;

    public HtmlPreviewView() {
        getStyleClass().add("preview-content");

        webView = new WebView();
        webView.getEngine().setJavaScriptEnabled(false);
        webView.setPrefHeight(Double.MAX_VALUE);

        setCenter(webView);
    }

    /** 加载 HTML 报告文件。 */
    public void loadReport(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            webView.getEngine().load(file.toURI().toString());
        }
    }

    /** 获取内部 WebView（供外部高级操作）。 */
    public WebView getWebView() { return webView; }
}
