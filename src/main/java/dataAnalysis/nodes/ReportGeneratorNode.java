package dataAnalysis.nodes;

import dataAnalysis.AnalysisState;
import dataAnalysis.model.Insight;
import dataAnalysis.model.ReportData;
import dataAnalysis.report.HtmlReportGenerator;
import org.bsc.langgraph4j.action.NodeAction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 报告生成节点
 * 从状态中收集所有分析数据，生成精美 HTML 报告
 */
public class ReportGeneratorNode implements NodeAction<AnalysisState> {

    private static final DateTimeFormatter DISPLAY_TIME =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Map<String, Object> apply(AnalysisState state) {
        System.out.println("=== [ReportGeneratorNode] 开始执行 ===");

        try {
            var profile = state.dataProfile();
            var insights = state.insights();
            var chartEmbeds = state.chartEmbeds();
            var analysisResults = state.analysisResults();

            // 构建分析章节
            List<ReportData.AnalysisSection> sections = buildAnalysisSections(analysisResults);

            // 构建建议列表
            List<String> recommendations = buildRecommendations(insights);

            // 组装报告数据
            ReportData reportData = new ReportData(
                profile,
                insights,
                chartEmbeds,
                sections,
                recommendations,
                LocalDateTime.now().format(DISPLAY_TIME),
                state.csvPath()
            );

            // 生成 HTML 报告
            String reportPath = HtmlReportGenerator.generate(reportData);
            System.out.println("HTML 报告已生成: " + reportPath);

            return Map.of(
                AnalysisState.REPORT_PATH_KEY, reportPath,
                AnalysisState.CURRENT_STEP_KEY, "REPORT_GENERATED"
            );
        } catch (Exception e) {
            System.err.println("报告生成失败: " + e.getMessage());
            e.printStackTrace();
            return Map.of(
                AnalysisState.ERRORS_KEY, List.of("报告生成失败: " + e.getMessage()),
                AnalysisState.CURRENT_STEP_KEY, "ERROR"
            );
        }
    }

    /**
     * 从分析结果构建章节列表
     */
    private List<ReportData.AnalysisSection> buildAnalysisSections(List<Map<String, String>> results) {
        List<ReportData.AnalysisSection> sections = new ArrayList<>();
        if (results == null) return sections;

        for (Map<String, String> result : results) {
            String title = result.getOrDefault("target", "未命名分析");
            String content = result.getOrDefault("result", "");
            String taskId = result.getOrDefault("taskId", "");
            sections.add(new ReportData.AnalysisSection(title, content, taskId));
        }
        return sections;
    }

    /**
     * 从洞察中提取建议
     */
    private List<String> buildRecommendations(List<Insight> insights) {
        List<String> recommendations = new ArrayList<>();
        if (insights == null) return recommendations;

        for (Insight insight : insights) {
            if (insight.recommendation() != null && !insight.recommendation().isBlank()) {
                recommendations.add(insight.recommendation());
            }
        }
        return recommendations;
    }
}
