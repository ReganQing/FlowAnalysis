package dataAnalysis.agents;

import dataAnalysis.AnalysisState;
import dataAnalysis.tools.ReportTools;

import java.util.List;
import java.util.Map;

/**
 * 报告生成智能体
 * 负责生成最终的数据分析报告
 */
public class ReportGeneratorAgent {

    private final ReportTools reportTools;

    public ReportGeneratorAgent() {
        this.reportTools = new ReportTools();
    }

    public Map<String, Object> apply(AnalysisState state) {
        System.out.println("=== ReportGeneratorAgent 开始执行 ===");

        try {
            Map<String, String> analysisResult = state.analysisResult();
            List<String> chartPaths = state.chartPaths();
            String dataSummary = state.dataSummary();

            // 检查是否有分析结果
            if (analysisResult == null || analysisResult.isEmpty()) {
                // 生成基础报告
                String reportPath = reportTools.generateMarkdownReport(
                        "{}",
                        chartPaths,
                        dataSummary
                );
                System.out.println("基础报告已生成: " + reportPath);

                return Map.of(
                        AnalysisState.REPORT_PATH_KEY, reportPath,
                        AnalysisState.CURRENT_STEP_KEY, "REPORT_GENERATED"
                );
            }

            // 提取分析结果
            String dataOverview = analysisResult.getOrDefault("descriptive_stats", "无数据");
            String trendAnalysis = analysisResult.getOrDefault("trend_analysis", "无趋势数据");
            String productRanking = analysisResult.getOrDefault("product_ranking", "无排行数据");
            String regionalAnalysis = analysisResult.getOrDefault("regional_analysis", "无区域数据");
            String channelAnalysis = analysisResult.getOrDefault("channel_comparison", "无渠道数据");

            // 生成核心发现和业务建议 (简化版，实际可以用 AI 生成)
            String keyFindings = generateKeyFindings(analysisResult);
            String recommendations = generateRecommendations(analysisResult);

            // 生成销售数据专用报告
            String reportPath = reportTools.generateSalesReport(
                    dataOverview,
                    trendAnalysis,
                    productRanking,
                    regionalAnalysis,
                    channelAnalysis,
                    chartPaths,
                    keyFindings,
                    recommendations
            );

            System.out.println("完整分析报告已生成: " + reportPath);

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
     * 生成核心发现 (简化版)
     */
    private String generateKeyFindings(Map<String, String> analysisResult) {
        StringBuilder findings = new StringBuilder();

        findings.append("**核心发现:**\n\n");

        // 基于分析结果生成发现
        if (analysisResult.containsKey("product_ranking")) {
            findings.append("- Top 产品表现突出，销售额占比显著\n");
        }

        if (analysisResult.containsKey("trend_analysis")) {
            findings.append("- 销售趋势呈现明显的时间特征\n");
        }

        if (analysisResult.containsKey("regional_analysis")) {
            findings.append("- 区域销售分布不均衡，存在优化空间\n");
        }

        if (analysisResult.containsKey("channel_comparison")) {
            findings.append("- 线上线下渠道表现各有优势\n");
        }

        if (analysisResult.containsKey("correlation")) {
            findings.append("- 销量与销售额存在强相关性\n");
        }

        return findings.toString();
    }

    /**
     * 生成业务建议 (简化版)
     */
    private String generateRecommendations(Map<String, String> analysisResult) {
        StringBuilder recommendations = new StringBuilder();

        recommendations.append("**业务建议:**\n\n");

        recommendations.append("1. **产品策略**\n");
        recommendations.append("   - 重点推广 Top 产品，提升核心产品曝光\n");
        recommendations.append("   - 分析低销量产品原因，考虑优化或下架\n\n");

        recommendations.append("2. **渠道优化**\n");
        recommendations.append("   - 根据渠道表现调整资源分配\n");
        recommendations.append("   - 加强线上渠道的营销投入\n\n");

        recommendations.append("3. **区域拓展**\n");
        recommendations.append("   - 重点区域加大市场投入\n");
        recommendations.append("   - 低渗透区域制定拓展计划\n\n");

        recommendations.append("4. **库存管理**\n");
        recommendations.append("   - 根据销售趋势优化库存结构\n");
        recommendations.append("   - 避免热销产品缺货\n\n");

        return recommendations.toString();
    }
}
