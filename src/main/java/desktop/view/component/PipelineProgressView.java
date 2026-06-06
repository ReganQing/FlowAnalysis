package desktop.view.component;

import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * 管线进度指示器 — 7 阶段横向进度条。
 * <p>
 * 阶段：Parse → Clean → Plan → Analyze → Insight → Chart → Report
 * 每个阶段有 4 种状态：PENDING / ACTIVE / COMPLETED / ERROR
 */
public class PipelineProgressView extends HBox {

    public enum StageStatus { PENDING, ACTIVE, COMPLETED, ERROR }

    private static final String[] STAGE_NAMES = {"Parse", "Clean", "Plan", "Analyze", "Insight", "Chart", "Report"};
    private static final String[] STAGE_LABELS = {"解析", "清洗", "规划", "分析", "洞察", "图表", "报告"};

    private static final String COLOR_PENDING = "#475569";
    private static final String COLOR_ACTIVE  = "#2563EB";
    private static final String COLOR_DONE    = "#059669";
    private static final String COLOR_ERROR   = "#EF4444";

    private final StageView[] stages = new StageView[7];
    private ScaleTransition activeAnimation;

    public PipelineProgressView() {
        getStyleClass().add("pipeline-progress");
        setAlignment(Pos.CENTER);
        setSpacing(0);

        for (int i = 0; i < 7; i++) {
            if (i > 0) {
                // 连接线
                Label connector = new Label();
                connector.setStyle("-fx-background-color: " + COLOR_PENDING + ";"
                    + " -fx-pref-height: 2; -fx-pref-width: 24; -fx-background-radius: 1;");
                getChildren().add(connector);
            }
            StageView stage = new StageView(STAGE_LABELS[i]);
            stages[i] = stage;
            getChildren().add(stage);
        }
    }

    /** 更新指定阶段的状态。 */
    public void updateStage(int index, StageStatus status) {
        if (index < 0 || index >= 7) return;

        // 停止之前的动画
        if (activeAnimation != null) {
            activeAnimation.stop();
            activeAnimation = null;
        }

        stages[index].setStatus(status);

        // 更新已通过的阶段和连接线颜色
        for (int i = 0; i < index; i++) {
            stages[i].setStatus(StageStatus.COMPLETED);
        }

        // 更新连接线颜色
        int connectorIndex = 0;
        for (int i = 1; i < 7; i++) {
            if (i <= index) {
                int ci = connectorIndex;
                if (getChildren().get(ci * 2 + 1) instanceof Label connector) {
                    connector.setStyle("-fx-background-color: " + COLOR_DONE + ";"
                        + " -fx-pref-height: 2; -fx-pref-width: 24; -fx-background-radius: 1;");
                }
            }
            connectorIndex++;
        }

        // ACTIVE 阶段添加脉冲动画
        if (status == StageStatus.ACTIVE) {
            activeAnimation = new ScaleTransition(Duration.millis(800), stages[index].indicator);
            activeAnimation.setFromX(1.0);
            activeAnimation.setFromY(1.0);
            activeAnimation.setToX(1.3);
            activeAnimation.setToY(1.3);
            activeAnimation.setCycleCount(ScaleTransition.INDEFINITE);
            activeAnimation.setAutoReverse(true);
            activeAnimation.play();
        }
    }

    /** 重置所有阶段为 PENDING。 */
    public void reset() {
        if (activeAnimation != null) {
            activeAnimation.stop();
            activeAnimation = null;
        }
        for (int i = 0; i < 7; i++) {
            stages[i].setStatus(StageStatus.PENDING);
        }
        // 重置连接线
        for (int i = 1; i < getChildren().size(); i += 2) {
            if (getChildren().get(i) instanceof Label connector) {
                connector.setStyle("-fx-background-color: " + COLOR_PENDING + ";"
                    + " -fx-pref-height: 2; -fx-pref-width: 24; -fx-background-radius: 1;");
            }
        }
    }

    /** 获取阶段总数。 */
    public int getStageCount() { return 7; }

    // ── 内部阶段视图 ──────────────────────────────────────────

    private static class StageView extends VBox {
        final Circle indicator;

        StageView(String label) {
            setAlignment(Pos.CENTER);
            setSpacing(4);

            indicator = new Circle(6);
            indicator.setFill(Color.web(COLOR_PENDING));

            Label text = new Label(label);
            text.setStyle("-fx-font-size: 11px; -fx-font-family: 'Poppins', sans-serif;"
                + " -fx-text-fill: " + COLOR_PENDING + ";");

            getChildren().addAll(indicator, text);
        }

        void setStatus(StageStatus status) {
            String color = switch (status) {
                case PENDING   -> COLOR_PENDING;
                case ACTIVE    -> COLOR_ACTIVE;
                case COMPLETED -> COLOR_DONE;
                case ERROR     -> COLOR_ERROR;
            };

            indicator.setFill(Color.web(color));

            // 更新标签颜色和字重
            if (getChildren().get(1) instanceof Label text) {
                text.setStyle("-fx-font-size: 11px; -fx-font-family: 'Poppins', sans-serif;"
                    + " -fx-text-fill: " + color + ";"
                    + (status == StageStatus.ACTIVE ? " -fx-font-weight: 600;" : ""));
            }
        }
    }
}
