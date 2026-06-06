package desktop.view.component;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 文件预览面板 — 右栏，可切换预览 HTML 报告、图表图片、数据表格。
 * <p>
 * 包含顶部标签栏和中部内容区，空状态时显示引导文字。
 */
public class FilePreviewPanel extends BorderPane {

    private final PreviewTabBar tabBar;
    private final StackPane contentArea;
    private final VBox emptyState;

    /** 缓存每个文件的预览组件 */
    private final Map<String, javafx.scene.Node> previewCache = new LinkedHashMap<>();

    public FilePreviewPanel() {
        getStyleClass().add("file-preview-panel");

        // ── 顶部标签栏 ──
        tabBar = new PreviewTabBar();
        tabBar.setOnTabSelected(this::switchPreview);
        setTop(tabBar);

        // ── 中部内容区 ──
        contentArea = new StackPane();
        contentArea.getStyleClass().add("preview-content");
        setCenter(contentArea);

        // ── 空状态 ──
        emptyState = new VBox(12);
        emptyState.getStyleClass().add("preview-empty-state");
        emptyState.setAlignment(Pos.CENTER);

        Label icon = new Label("📊");
        icon.getStyleClass().add("preview-empty-icon");
        Label text = new Label("上传 Excel 文件开始数据分析");
        text.getStyleClass().add("preview-empty-text");
        Label hint = new Label("分析结果将在此处展示");
        hint.getStyleClass().add("preview-empty-hint");

        emptyState.getChildren().addAll(icon, text, hint);
        contentArea.getChildren().add(emptyState);
    }

    /**
     * 添加一个文件到预览面板。
     *
     * @param filePath 文件路径
     */
    public void addFile(String filePath) {
        if (filePath == null || !new File(filePath).exists()) return;

        String fileName = new File(filePath).getName();
        String extension = getExtension(fileName).toLowerCase();

        // 创建对应的预览组件
        javafx.scene.Node preview = createPreview(filePath, extension);
        if (preview == null) return;

        previewCache.put(filePath, preview);
        tabBar.addTab(getFileLabel(fileName, extension), filePath);
    }

    /** 清除所有文件。 */
    public void clear() {
        tabBar.clear();
        previewCache.clear();
        contentArea.getChildren().clear();
        contentArea.getChildren().add(emptyState);
    }

    /** 获取标签栏（供外部控制）。 */
    public PreviewTabBar getTabBar() { return tabBar; }

    // ── 内部方法 ──────────────────────────────────────────────

    private void switchPreview(String filePath) {
        javafx.scene.Node preview = previewCache.get(filePath);
        contentArea.getChildren().clear();
        if (preview != null) {
            contentArea.getChildren().add(preview);
        } else {
            contentArea.getChildren().add(emptyState);
        }
    }

    private javafx.scene.Node createPreview(String filePath, String extension) {
        return switch (extension) {
            case "html", "htm" -> {
                HtmlPreviewView view = new HtmlPreviewView();
                view.loadReport(filePath);
                yield view;
            }
            case "png", "jpg", "jpeg", "gif" -> {
                ImagePreviewView view = new ImagePreviewView();
                view.loadImage(filePath);
                yield view;
            }
            case "csv" -> {
                CsvPreviewView view = new CsvPreviewView();
                view.loadCsv(filePath);
                yield view;
            }
            default -> {
                Label placeholder = new Label("不支持的预览格式: ." + extension);
                placeholder.setStyle("-fx-text-fill: #6B7685; -fx-padding: 24;");
                yield placeholder;
            }
        };
    }

    private static String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot + 1) : "";
    }

    private static String getFileLabel(String fileName, String extension) {
        // 根据文件类型选择图标
        String icon = switch (extension.toLowerCase()) {
            case "html", "htm" -> "📄 ";
            case "png", "jpg", "jpeg" -> "📈 ";
            case "csv", "xls", "xlsx" -> "📊 ";
            default -> "📎 ";
        };
        // 截断过长的文件名
        if (fileName.length() > 20) {
            return icon + fileName.substring(0, 17) + "...";
        }
        return icon + fileName;
    }
}
