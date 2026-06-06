package dataAnalysis.nodes;

import dataAnalysis.AnalysisState;
import dataAnalysis.model.ChartEmbed;
import dataAnalysis.tools.BaseTools;
import dataAnalysis.tools.ChartTools;
import org.bsc.langgraph4j.action.NodeAction;
import tech.tablesaw.api.Table;
import tech.tablesaw.aggregate.AggregateFunctions;

import java.util.*;

/**
 * 图表生成节点
 */
public class ChartGeneratorNode implements NodeAction<AnalysisState> {

    private final ChartTools chartTools = new ChartTools();

    @Override
    public Map<String, Object> apply(AnalysisState state) {
        System.out.println("=== [ChartGeneratorNode] 开始执行 ===");

        List<ChartEmbed> charts = new ArrayList<>();

        try {
            String csvPath = state.csvPath();
            Table table = BaseTools.loadCSVTable(csvPath);

            // 统计图表
            if (table.containsColumn("amount")) {
                try {
                    String chartPath = chartTools.createStatsChart(table, "amount");
                    String base64 = encodeImageToBase64(chartPath);
                    charts.add(new ChartEmbed("销售额统计", base64, "销售额的均值、中位数、最小值、最大值", chartPath));
                    System.out.println("统计图表已生成: " + chartPath);
                } catch (Exception e) {
                    System.err.println("统计图表生成失败: " + e.getMessage());
                }
            }

            // 区域柱状图
            if (table.containsColumn("region") && table.containsColumn("amount")) {
                try {
                    var regionData = table.summarize("amount", AggregateFunctions.sum)
                            .by("region");
                    String labels = String.join(",", regionData.column(0).asStringColumn().asList());
                    var amountCol = regionData.numberColumn(1);
                    StringBuilder values = new StringBuilder();
                    for (int i = 0; i < amountCol.size(); i++) {
                        if (i > 0) values.append(",");
                        values.append(String.format("%.0f", amountCol.getDouble(i)));
                    }
                    String chartPath = chartTools.createSimpleBarChart("区域销售额", labels, values.toString());
                    String base64 = encodeImageToBase64(chartPath);
                    charts.add(new ChartEmbed("区域销售分布", base64, "各区域销售额对比", chartPath));
                    System.out.println("区域图表已生成: " + chartPath);
                } catch (Exception e) {
                    System.err.println("区域图表生成失败: " + e.getMessage());
                }
            }

            System.out.println("共生成 " + charts.size() + " 个图表");

            return Map.of(
                AnalysisState.CHART_EMBEDS_KEY, charts,
                AnalysisState.CURRENT_STEP_KEY, "CHARTS_GENERATED"
            );
        } catch (Exception e) {
            System.err.println("图表生成失败: " + e.getMessage());
            return Map.of(
                AnalysisState.ERRORS_KEY, List.of("图表生成失败: " + e.getMessage()),
                AnalysisState.CURRENT_STEP_KEY, "ERROR"
            );
        }
    }

    private String encodeImageToBase64(String imagePath) {
        try {
            byte[] bytes = java.nio.file.Files.readAllBytes(java.nio.file.Path.of(imagePath));
            return java.util.Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            return "";
        }
    }
}
