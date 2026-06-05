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
    String dataSource
) {
    public record AnalysisSection(
        String title,
        String content,
        String chartId
) {}
}
