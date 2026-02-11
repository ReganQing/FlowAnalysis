package dataAnalysis.agents;

import dataAnalysis.AnalysisState;
import dataAnalysis.tools.ChartTools;
import dataAnalysis.tools.BaseTools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 图表生成智能体
 * 负责根据分析结果生成可视化图表
 */
public class ChartGeneratorAgent {

    private final ChartTools chartTools;

    public ChartGeneratorAgent() {
        this.chartTools = new ChartTools();
    }

    public Map<String, Object> apply(AnalysisState state) {
        System.out.println("=== ChartGeneratorAgent 开始执行 ===");

        List<String> chartPaths = new ArrayList<>();

        try {
            String csvPath = state.csvPath();
            tech.tablesaw.api.Table table = BaseTools.loadCSVTable(csvPath);

            // 生成统计图表
            if (table.containsColumn("amount")) {
                System.out.println("生成销售额统计图表...");
                try {
                    String chartPath = chartTools.createStatsChart(
                            table,
                            "amount"
                    );
                    chartPaths.add(chartPath);
                    System.out.println("图表已保存: " + chartPath);
                } catch (Exception e) {
                    System.err.println("生成统计图表失败: " + e.getMessage());
                }
            }

            // 注意: 更复杂的图表生成方法需要后续实现
            // 目前只生成基础的统计图表

            String summary = String.format("图表生成完成\n\n共生成 %d 个图表:\n%s",
                    chartPaths.size(),
                    String.join("\n", chartPaths));

            System.out.println(summary);

            return Map.of(
                    AnalysisState.CHART_PATHS_KEY, chartPaths,
                    AnalysisState.CURRENT_STEP_KEY, "CHARTS_GENERATED",
                    AnalysisState.DATA_SUMMARY_KEY, state.dataSummary() + "\n\n" + summary
            );

        } catch (Exception e) {
            System.err.println("图表生成失败: " + e.getMessage());
            e.printStackTrace();
            return Map.of(
                    AnalysisState.ERRORS_KEY, List.of("图表生成失败: " + e.getMessage()),
                    AnalysisState.CURRENT_STEP_KEY, "ERROR"
            );
        }
    }
}
