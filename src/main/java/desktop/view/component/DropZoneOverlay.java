package desktop.view.component;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * 拖放文件覆盖层 — 在用户拖拽文件到聊天区域时显示。
 * <p>
 * 默认不可见，通过 {@link #show()} / {@link #hide()} 控制显隐。
 */
public class DropZoneOverlay extends StackPane {

    public DropZoneOverlay() {
        getStyleClass().add("drop-zone-overlay");
        setVisible(false);
        setMouseTransparent(true);

        VBox content = new VBox(8);
        content.setAlignment(Pos.CENTER);

        Label icon = new Label("☁");
        icon.getStyleClass().add("drop-zone-icon");

        Label text = new Label("拖放 Excel 文件到此处");
        text.getStyleClass().add("drop-zone-text");

        Label hint = new Label("支持 .csv .xls .xlsx，最大 50MB");
        hint.getStyleClass().add("drop-zone-hint");

        content.getChildren().addAll(icon, text, hint);
        getChildren().add(content);
    }

    public void show() {
        setVisible(true);
        setMouseTransparent(false);
    }

    public void hide() {
        setVisible(false);
        setMouseTransparent(true);
    }
}
