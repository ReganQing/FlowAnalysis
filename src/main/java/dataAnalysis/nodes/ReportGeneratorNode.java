package dataAnalysis.nodes;

import dataAnalysis.AnalysisState;
import dataAnalysis.model.DataProfile;
import dataAnalysis.model.Insight;
import dataAnalysis.model.ReportData;
import dataAnalysis.report.HtmlReportGenerator;
import dataAnalysis.router.ModelRouter;
import dev.langchain4j.model.chat.ChatModel;
import org.bsc.langgraph4j.action.NodeAction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 报告生成节点
 * 从状态中收集所有分析数据，使用 AI 生成执行摘要，输出精美 HTML 报告
 */
public class ReportGeneratorNode implements NodeAction<AnalysisState> {

    private static final DateTimeFormatter DISPLAY_TIME =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ChatModel model;

    public ReportGeneratorNode(ModelRouter modelRouter) {
        this.model = modelRouter.getModelForTask(ModelRouter.TaskType.REPORT);
    }

    @Override
    public Map<String, Object> apply(AnalysisState state) {
        System.out.println("=== [ReportGeneratorNode] 开始执行 ===");

        try {
            var profile = state.dataProfile();
            var insights = state.insights();
            var chartEmbeds = state.chartEmbeds();
            var analysisResults = state.analysisResults();

            // 构建分析章节（含 AI 叙述）
            List<ReportData.AnalysisSection> sections = buildAnalysisSections(analysisResults);

            // 构建建议列表
            List<String> recommendations = buildRecommendations(insights);

            // AI 生成执行摘要
            String executiveSummary = generateExecutiveSummary(analysisResults, insights, profile);
            if (executiveSummary != null) {
                System.out.println("执行摘要已生成: " + (executiveSummary.length() > 100
                    ? executiveSummary.substring(0, 100) + "..."
                    : executiveSummary));
            }

            // 组装报告数据
            ReportData reportData = new ReportData(
                profile,
                insights,
                chartEmbeds,
                sections,
                recommendations,
                LocalDateTime.now().format(DISPLAY_TIME),
                state.csvPath(),
                executiveSummary
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
     * 使用 AI 生成执行摘要
     * 综合所有分析结果和洞察，生成面向管理层的概述
     *
     * @return 执行摘要文本，失败时返回 null（HtmlReportGenerator 会跳过空摘要）
     */
    private String generateExecutiveSummary(List<Map<String, String>> results,
                                             List<Insight> insights,
                                             DataProfile profile) {
        try {
            StringBuilder context = new StringBuilder();
            context.append("数据概况: ").append(profile.rowCount()).append("行 × ")
                   .append(profile.columnCount()).append("列\n\n");

            if (results != null) {
                for (Map<String, String> r : results) {
                    context.append("[").append(r.getOrDefault("type", ""))
                           .append("] ").append(r.getOrDefault("target", "")).append("\n");
                    // 优先使用 AI 叙述，降级到原始结果
                    context.append(r.getOrDefault("narrative", r.getOrDefault("result", "")))
                           .append("\n\n");
                }
            }

            if (insights != null) {
                for (Insight i : insights) {
                    context.append("洞察[").append(i.severity().name()).append("]: ")
                           .append(i.title()).append(" - ").append(i.explanation()).append("\n");
                }
            }

            // 防止 context 过长超出模型 Token 上限，截断至 4000 字符
            String contextStr = context.toString();
            if (contextStr.length() > 4000) {
                contextStr = contextStr.substring(0, 4000) + "\n\n...(内容已截断)";
                System.out.println("执行摘要上下文过长(" + context.length() + "字符)，已截断至4000字符");
            }

            String prompt = """
                你是资深数据分析顾问。请根据以下分析结果，撰写一份执行摘要。

                %s

                要求：
                1. 3-5句话，概括最核心的发现和业务影响
                2. 开篇点明数据规模和范围
                3. 突出最重要的发现和异常
                4. 以一句话给出最重要的行动建议
                5. 语气专业、简洁，面向管理层
                6. 只输出摘要文本，不要其他内容
                """.formatted(contextStr);

            return model.chat(prompt);
        } catch (Exception e) {
            System.err.println("执行摘要生成失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 从分析结果构建章节列表
     * 优先使用 DataAnalyzerNode 生成的 AI 叙述
     */
    private List<ReportData.AnalysisSection> buildAnalysisSections(List<Map<String, String>> results) {
        List<ReportData.AnalysisSection> sections = new ArrayList<>();
        if (results == null) return sections;

        for (Map<String, String> result : results) {
            String title = result.getOrDefault("target", "未命名分析");
            String content = result.getOrDefault("result", "");
            String narrative = result.getOrDefault("narrative", "");
            String taskId = result.getOrDefault("taskId", "");

            sections.add(new ReportData.AnalysisSection(title, content, narrative, taskId));
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
