package desktop.view.component;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * 工具调用卡片 — 展示工具名称、入参、结果、耗时。
 */
public class ToolCallCard extends VBox {

    public ToolCallCard(String toolName, String toolInput,
                        String result, long durationMs, boolean success) {
        getStyleClass().add("tool-card");
        setSpacing(8);
        setMaxWidth(560);

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label("🔧 " + toolName);
        name.getStyleClass().add("tool-card-header");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label duration = new Label(durationMs + "ms");
        duration.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11px;");

        Label status = new Label(success ? "✅ 成功" : "❌ 失败");
        status.getStyleClass().add(success
            ? "tool-card-status-success" : "tool-card-status-error");

        header.getChildren().addAll(name, spacer, duration, status);

        Label inputLabel = new Label("输入参数");
        inputLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");
        Label inputCode = new Label(toolInput);
        inputCode.getStyleClass().add("tool-card-input");
        inputCode.setWrapText(true);
        inputCode.setMaxWidth(Double.MAX_VALUE);

        Label resultLabel = new Label("返回结果");
        resultLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");
        Label resultText = new Label(result);
        resultText.getStyleClass().add("tool-card-result");
        resultText.setWrapText(true);

        getChildren().addAll(header, inputLabel, inputCode, resultLabel, resultText);
    }
}
