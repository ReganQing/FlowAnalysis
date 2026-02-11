package dataAnalysis.agents;

import dataAnalysis.AnalysisState;
import dataAnalysis.tools.AnalysisTools;
import dataAnalysis.tools.BaseTools;

import java.util.List;
import java.util.Map;

/**
 * 数据分析智能体
 * 负责执行多维度数据分析
 */
public class DataAnalyzerAgent {

    private final AnalysisTools analysisTools;

    public DataAnalyzerAgent() {
        this.analysisTools = new AnalysisTools();
    }

    public Map<String, Object> apply(AnalysisState state) {
        System.out.println("=== DataAnalyzerAgent 开始执行 ===");

        try {
            String csvPath = state.csvPath();
            System.out.println("分析文件: " + csvPath);

            // 加载数据
            tech.tablesaw.api.Table table = BaseTools.loadCSVTable(csvPath);

            StringBuilder allResults = new StringBuilder();
            Map<String, String> analysisMap = new java.util.HashMap<>();

            // 1. 描述性统计
            System.out.println("执行描述性统计...");
            String descriptiveStats = analysisTools.descriptiveStats(table);
            allResults.append("### 描述性统计\n\n").append(descriptiveStats).append("\n\n");
            analysisMap.put("descriptive_stats", descriptiveStats);

            // 2. 销售趋势分析 (如果有 date 和 amount 列)
            if (table.containsColumn("date") && table.containsColumn("amount")) {
                System.out.println("执行销售趋势分析...");
                String trendAnalysis = analysisTools.salesTrendAnalysis(table, "date", "amount", "month");
                allResults.append("### 销售趋势\n\n").append(trendAnalysis).append("\n\n");
                analysisMap.put("trend_analysis", trendAnalysis);
            }

            // 3. 产品销售排行
            if (table.containsColumn("product_name") && table.containsColumn("amount")) {
                System.out.println("执行产品排行分析...");
                String productRanking = analysisTools.topSellingProducts(table, "product_id", "product_name", "amount", 10);
                allResults.append("### 产品排行 Top 10\n\n").append(productRanking).append("\n\n");
                analysisMap.put("product_ranking", productRanking);
            }

            // 4. 区域销售分析
            if (table.containsColumn("region") && table.containsColumn("amount")) {
                System.out.println("执行区域销售分析...");
                String regionalAnalysis = analysisTools.regionalSalesAnalysis(table, "region", "amount");
                allResults.append("### 区域销售分析\n\n").append(regionalAnalysis).append("\n\n");
                analysisMap.put("regional_analysis", regionalAnalysis);
            }

            // 5. 渠道对比分析
            if (table.containsColumn("sales_channel") && table.containsColumn("amount")) {
                System.out.println("执行渠道对比分析...");
                String channelComparison = analysisTools.channelComparison(table, "sales_channel", "amount");
                allResults.append("### 渠道对比分析\n\n").append(channelComparison).append("\n\n");
                analysisMap.put("channel_comparison", channelComparison);
            }

            // 6. 相关性分析 (选择数值列)
            if (table.containsColumn("quantity") && table.containsColumn("amount")) {
                System.out.println("执行相关性分析...");
                String correlation = analysisTools.correlationAnalysis(table, "quantity", "amount");
                allResults.append("### 相关性分析\n\n").append(correlation).append("\n\n");
                analysisMap.put("correlation", correlation);
            }

            String summary = String.format("数据分析完成\n\n执行的分析:\n1. 描述性统计\n2. 销售趋势分析\n3. 产品排行\n4. 区域分析\n5. 渠道对比\n6. 相关性分析\n\n详细结果:\n\n%s",
                    allResults.toString());

            System.out.println(summary);

            return Map.of(
                    AnalysisState.ANALYSIS_RESULT_KEY, analysisMap,
                    AnalysisState.CURRENT_STEP_KEY, "DATA_ANALYZED",
                    AnalysisState.DATA_SUMMARY_KEY, state.dataSummary() + "\n\n" + summary
            );

        } catch (Exception e) {
            System.err.println("数据分析失败: " + e.getMessage());
            e.printStackTrace();
            return Map.of(
                    AnalysisState.ERRORS_KEY, List.of("数据分析失败: " + e.getMessage()),
                    AnalysisState.CURRENT_STEP_KEY, "ERROR"
            );
        }
    }
}
