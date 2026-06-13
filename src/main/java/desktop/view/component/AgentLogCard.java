package desktop.view.component;

import javafx.animation.RotateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Agent 日志卡片 — 可折叠的单节点日志展示。
 * <p>
 * 每个管线节点对应一张卡片，显示该节点的思考/行动/结果/错误日志。
 * 完成后自动折叠，进行中自动展开。
 */
public class AgentLogCard extends VBox {

    public enum LogLevel { THINKING, ACTION, RESULT, ERROR, DEBUG }

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    /** 节点名 → 中文展示名。 */
    private static final java.util.Map<String, String> DISPLAY_NAMES = java.util.Map.of(
        "parser", "数据解析",
        "cleaner", "数据清洗",
        "planner", "智能规划",
        "analyzer", "数据分析",
        "insight", "深度洞察",
        "chart", "图表生成",
        "report", "报告生成"
    );

    private final VBox logBody;
    private final VBox subTaskBox;
    private final java.util.Map<String, Label> subTaskLabels = new java.util.LinkedHashMap<>();
    private final Label statusLabel;
    private final Label durationLabel;
    private final Label toggleButton;
    private boolean expanded = true;

    public AgentLogCard(String nodeName, int stageIndex) {
        getStyleClass().add("agent-log-card");
        setSpacing(6);

        // ── 头部 ──
        HBox header = new HBox(8);
        header.getStyleClass().add("agent-log-header");
        header.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(DISPLAY_NAMES.getOrDefault(nodeName, nodeName));
        nameLabel.getStyleClass().add("agent-log-name");

        Label stageLabel = new Label("阶段 " + stageIndex + "/7");
        stageLabel.getStyleClass().add("agent-log-stage");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        statusLabel = new Label("⏳");
        statusLabel.setStyle("-fx-font-size: 12px;");

        durationLabel = new Label();
        durationLabel.getStyleClass().add("agent-log-duration");

        toggleButton = new Label("▼");
        toggleButton.getStyleClass().add("agent-log-toggle");
        toggleButton.setOnMouseClicked(e -> toggleExpand());

        header.getChildren().addAll(nameLabel, stageLabel, spacer, statusLabel, durationLabel, toggleButton);

        // ── 子步骤区（迭代节点用）──
        subTaskBox = new VBox(2);
        subTaskBox.getStyleClass().add("agent-log-body");
        subTaskBox.setPadding(new javafx.geometry.Insets(4, 0, 0, 20));

        // ── 日志主体（可折叠）──
        logBody = new VBox(2);
        logBody.getStyleClass().add("agent-log-body");
        logBody.setPadding(new javafx.geometry.Insets(4, 0, 0, 20));

        getChildren().addAll(header, subTaskBox, logBody);
    }

    /** 添加一条日志。 */
    public void addLog(String message, LogLevel level) {
        String time = LocalTime.now().format(TIME_FMT);
        String prefix = switch (level) {
            case THINKING -> "💭 ";
            case ACTION   -> "⚡ ";
            case RESULT   -> "✓ ";
            case ERROR    -> "✗ ";
            case DEBUG    -> "• ";
        };

        Label line = new Label(time + "  " + prefix + message);
        line.getStyleClass().addAll("log-line", cssClass(level));
        line.setWrapText(true);
        logBody.getChildren().add(line);

        ensureExpanded();
    }

    /**
     * 添加/更新一个子步骤行。同一 label 多次调用会更新其状态图标。
     */
    public void addSubTask(int index, int total, String label, String statusGlyph) {
        Label line = subTaskLabels.computeIfAbsent(label, k -> {
            Label l = new Label();
            l.getStyleClass().addAll("log-line", "log-result");
            l.setWrapText(true);
            subTaskBox.getChildren().add(l);
            return l;
        });
        line.setText(String.format("▸ %d/%d %s %s", index, total, label, statusGlyph));
        ensureExpanded();
    }

    /** 整阶段完成：标记 ✅ 并延迟折叠。区别于单任务完成。 */
    public void markPhaseComplete(long durationMs) {
        statusLabel.setText("✅");
        durationLabel.setText(durationMs + "ms");
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(Duration.millis(300));
        delay.setOnFinished(e -> collapse());
        delay.play();
    }

    private void ensureExpanded() {
        if (!expanded) {
            expanded = true;
            logBody.setVisible(true);
            logBody.setManaged(true);
            subTaskBox.setVisible(true);
            subTaskBox.setManaged(true);
            toggleButton.setText("▼");
        }
    }

    /** 标记节点完成，自动折叠。 */
    public void setCompleted(long durationMs) {
        statusLabel.setText("✅");
        durationLabel.setText(durationMs + "ms");

        // 300ms 后自动折叠
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(Duration.millis(300));
        delay.setOnFinished(e -> collapse());
        delay.play();
    }

    /** 标记节点错误。 */
    public void setError(String error) {
        statusLabel.setText("❌");
        addLog(error, LogLevel.ERROR);
        // 错误时保持展开
    }

    /** 手动设置展开/折叠。 */
    public void setExpanded(boolean expand) {
        if (expand) {
            expanded = true;
            logBody.setVisible(true);
            logBody.setManaged(true);
            toggleButton.setText("▼");
        } else {
            collapse();
        }
    }

    private void collapse() {
        expanded = false;
        logBody.setVisible(false);
        logBody.setManaged(false);
        subTaskBox.setVisible(false);
        subTaskBox.setManaged(false);
        toggleButton.setText("▶");
    }

    private void toggleExpand() {
        if (expanded) {
            collapse();
        } else {
            expanded = true;
            logBody.setVisible(true);
            logBody.setManaged(true);
            subTaskBox.setVisible(true);
            subTaskBox.setManaged(true);
            toggleButton.setText("▼");
        }
    }

    private static String cssClass(LogLevel level) {
        return switch (level) {
            case THINKING -> "log-thinking";
            case ACTION   -> "log-action";
            case RESULT   -> "log-result";
            case ERROR    -> "log-error";
            case DEBUG    -> "log-debug";
        };
    }
}
