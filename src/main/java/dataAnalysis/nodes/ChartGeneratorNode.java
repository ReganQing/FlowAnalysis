package dataAnalysis.nodes;

import dataAnalysis.AnalysisState;
import dataAnalysis.model.ChartEmbed;
import dataAnalysis.model.DataProfile;
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
            DataProfile profile = state.dataProfile();

            // 统计图：为第一个数值列生成描述性统计图
            if (profile != null && !profile.numericColumns().isEmpty()) {
                String numericCol = profile.numericColumns().get(0);
                if (table.containsColumn(numericCol)) {
                    try {
                        String chartPath = chartTools.createStatsChart(table, numericCol);
                        String base64 = encodeImageToBase64(chartPath);
                        charts.add(new ChartEmbed(numericCol + " 统计", base64,
                            numericCol + " 的均值、中位数、最小值、最大值", chartPath));
                        System.out.println("统计图表已生成: " + chartPath);
                    } catch (Exception e) {
                        System.err.println("统计图表生成失败: " + e.getMessage());
                    }
                }
            }

            // 分组柱状图：用第一个分类列对第一个数值列汇总
            if (profile != null
                    && !profile.categoricalColumns().isEmpty()
                    && !profile.numericColumns().isEmpty()) {
                String catCol = profile.categoricalColumns().get(0);
                String numCol = profile.numericColumns().get(0);
                if (table.containsColumn(catCol) && table.containsColumn(numCol)) {
                    try {
                        var grouped = table.summarize(numCol, AggregateFunctions.sum).by(catCol);
                        String labels = String.join(",", grouped.column(0).asStringColumn().asList());
                        var valueCol = grouped.numberColumn(1);
                        StringBuilder values = new StringBuilder();
                        for (int i = 0; i < valueCol.size(); i++) {
                            if (i > 0) values.append(",");
                            values.append(String.format("%.0f", valueCol.getDouble(i)));
                        }
                        String chartPath = chartTools.createSimpleBarChart(
                            "按 " + catCol + " 分组的 " + numCol, labels, values.toString());
                        String base64 = encodeImageToBase64(chartPath);
                        charts.add(new ChartEmbed(catCol + " 分组分布", base64,
                            "各 " + catCol + " 的 " + numCol + " 对比", chartPath));
                        System.out.println("分组图表已生成: " + chartPath);
                    } catch (Exception e) {
                        System.err.println("分组图表生成失败: " + e.getMessage());
                    }
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
