package dataAnalysis.model;

import java.util.List;

/**
 * HTML报告所需的所有数据
 */
public record ReportData(
    DataProfile profile,
    List<Insight> insights,
    List<ChartEmbed> charts,
    List<AnalysisSection> analysisSections,
    List<String> recommendations,
    String generatedAt,
    String dataSource,
    String executiveSummary
) {
    /**
     * 分析章节，包含原始统计结果和 AI 叙述文本
     */
    public record AnalysisSection(
        String title,
        String rawContent,
        String narrative,
        String chartId
    ) {
        /**
         * 向后兼容构造函数：narrative 为 null
         */
        public AnalysisSection(String title, String content, String chartId) {
            this(title, content, null, chartId);
        }

        /**
         * 获取展示内容：优先使用 AI 叙述，降级到原始内容
         */
        public String displayContent() {
            return (narrative != null && !narrative.isBlank()) ? narrative : rawContent;
        }
    }
}
