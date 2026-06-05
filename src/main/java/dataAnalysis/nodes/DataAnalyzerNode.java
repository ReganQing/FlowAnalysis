package dataAnalysis.nodes;

import dataAnalysis.AnalysisState;
import dataAnalysis.model.AnalysisPlan;
import dataAnalysis.model.AnalysisTask;
import dataAnalysis.model.AnalysisType;
import dataAnalysis.tools.AnalysisTools;
import dataAnalysis.tools.BaseTools;
import org.bsc.langgraph4j.action.NodeAction;
import tech.tablesaw.api.Table;

import java.util.*;

/**
 * 数据分析节点
 */
public class DataAnalyzerNode implements NodeAction<AnalysisState> {

    private final AnalysisTools analysisTools = new AnalysisTools();

    @Override
    public Map<String, Object> apply(AnalysisState state) {
        System.out.println("=== [DataAnalyzerNode] 开始执行 ===");

        try {
            String csvPath = state.csvPath();
            Table table = BaseTools.loadCSVTable(csvPath);
            AnalysisPlan plan = state.analysisPlan();

            if (plan == null) {
                return Map.of(
                    AnalysisState.ERRORS_KEY, List.of("无分析计划"),
                    AnalysisState.CURRENT_STEP_KEY, "ERROR"
                );
            }

            AnalysisTask task = plan.currentTask();
            if (task == null || task.type() == AnalysisType.INSIGHT) {
                System.out.println("当前无分析任务或任务为洞察类型，跳过");
                return Map.of(AnalysisState.CURRENT_STEP_KEY, "ANALYSIS_SKIPPED");
            }

            System.out.println("执行分析任务: " + task.target());

            String result = executeTask(table, task);
            System.out.println("分析结果: " + (result.length() > 200 ? result.substring(0, 200) + "..." : result));

            Map<String, String> resultMap = new LinkedHashMap<>();
            resultMap.put("taskId", task.taskId());
            resultMap.put("type", task.type().name());
            resultMap.put("target", task.target());
            resultMap.put("result", result);

            return Map.of(
                AnalysisState.ANALYSIS_RESULTS_KEY, resultMap,
                AnalysisState.CURRENT_STEP_KEY, "ANALYZED",
                AnalysisState.DATA_SUMMARY_KEY, state.dataSummary() + "\n\n### " + task.target() + "\n" + result
            );
        } catch (Exception e) {
            System.err.println("数据分析失败: " + e.getMessage());
            return Map.of(
                AnalysisState.ERRORS_KEY, List.of("数据分析失败: " + e.getMessage()),
                AnalysisState.CURRENT_STEP_KEY, "ERROR"
            );
        }
    }

    private String executeTask(Table table, AnalysisTask task) {
        var params = task.parameters();
        return switch (task.type()) {
            case TREND -> analysisTools.salesTrendAnalysis(
                table,
                params.getOrDefault("dateColumn", "date"),
                params.getOrDefault("valueColumn", "amount"),
                params.getOrDefault("interval", "month")
            );
            case DISTRIBUTION -> analysisTools.descriptiveStats(table);
            case CORRELATION -> analysisTools.correlationAnalysis(
                table,
                params.getOrDefault("col1", "quantity"),
                params.getOrDefault("col2", "amount")
            );
            case COMPARISON -> analysisTools.regionalSalesAnalysis(
                table,
                params.getOrDefault("groupColumn", "region"),
                params.getOrDefault("valueColumn", "amount")
            );
            case OUTLIER -> analysisTools.correlationAnalysis(
                table,
                params.getOrDefault("col1", "quantity"),
                params.getOrDefault("col2", "amount")
            );
            default -> "{\"note\": \"不支持的分析类型\"}";
        };
    }
}
