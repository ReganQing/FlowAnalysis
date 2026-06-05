package dataAnalysis.model;

/**
 * 洞察严重等级
 */
public enum InsightSeverity {
    CRITICAL("需立即关注", "#B85450"),
    WARNING("需留意", "#C89B3C"),
    INFO("一般信息", "#5A7A9A"),
    POSITIVE("积极信号", "#5A8A6A");

    private final String label;
    private final String color;

    InsightSeverity(String label, String color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() { return label; }
    public String getColor() { return color; }
}
