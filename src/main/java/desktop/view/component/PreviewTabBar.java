package desktop.view.component;

import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 文件预览标签栏 — 横向可滚动的文件标签。
 */
public class PreviewTabBar extends HBox {

    private final Map<String, ToggleButton> tabs = new LinkedHashMap<>();
    private Consumer<String> onTabSelected = path -> {};
    private String activePath;

    public PreviewTabBar() {
        getStyleClass().add("preview-tab-bar");
    }

    /** 添加一个文件标签。 */
    public void addTab(String label, String filePath) {
        if (tabs.containsKey(filePath)) return;

        ToggleButton tab = new ToggleButton(label);
        tab.getStyleClass().add("preview-tab");
        tab.setTooltip(new Tooltip(filePath));
        tab.setOnAction(e -> selectTab(filePath));

        tabs.put(filePath, tab);
        getChildren().add(tab);

        // 自动选中第一个
        if (tabs.size() == 1) {
            selectTab(filePath);
        }
    }

    /** 移除一个文件标签。 */
    public void removeTab(String filePath) {
        ToggleButton tab = tabs.remove(filePath);
        if (tab != null) {
            getChildren().remove(tab);
        }
        if (activePath != null && activePath.equals(filePath)) {
            // 选中下一个可用的
            if (!tabs.isEmpty()) {
                selectTab(tabs.keySet().iterator().next());
            } else {
                activePath = null;
            }
        }
    }

    /** 选中指定文件的标签。 */
    public void selectTab(String filePath) {
        tabs.values().forEach(t -> t.getStyleClass().remove("preview-tab-active"));
        ToggleButton tab = tabs.get(filePath);
        if (tab != null) {
            tab.getStyleClass().add("preview-tab-active");
            tab.setSelected(true);
            activePath = filePath;
            onTabSelected.accept(filePath);
        }
    }

    /** 清除所有标签。 */
    public void clear() {
        tabs.clear();
        getChildren().clear();
        activePath = null;
    }

    /** 获取当前选中的文件路径。 */
    public String getActivePath() { return activePath; }

    /** 设置标签选中回调。 */
    public void setOnTabSelected(Consumer<String> callback) {
        this.onTabSelected = callback;
    }
}
