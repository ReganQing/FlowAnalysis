package dataAnalysis.nodes;

import dataAnalysis.AnalysisState;
import dataAnalysis.ColumnSelection;
import dataAnalysis.model.AnalysisPlan;
import dataAnalysis.model.AnalysisTask;
import dataAnalysis.model.AnalysisType;
import dataAnalysis.model.DataProfile;
import dataAnalysis.router.ModelRouter;
import dataAnalysis.tools.AnalysisTools;
import dataAnalysis.tools.BaseTools;
import dev.langchain4j.model.chat.ChatModel;
import org.bsc.langgraph4j.action.NodeAction;
import tech.tablesaw.api.Table;

import java.util.*;

/**
 * 数据分析节点
 * 使用 AI 模型解读统计结果，生成中文叙述文本
 */
public class DataAnalyzerNode implements NodeAction<AnalysisState> {

    private final ChatModel model;
    private final AnalysisTools analysisTools = new AnalysisTools();

    public DataAnalyzerNode(ModelRouter modelRouter) {
        this.model = modelRouter.getModelForTask(ModelRouter.TaskType.ANALYSIS);
    }

    /** 仅供测试：不依赖 ModelRouter（executeTask 不使用 model）。 */
    DataAnalyzerNode() {
        this.model = null;
    }

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
                return Map.of(
                    AnalysisState.ANALYSIS_PLAN_KEY, plan.advance(),
                    AnalysisState.CURRENT_STEP_KEY, "ANALYSIS_SKIPPED"
                );
            }

            System.out.println("执行分析任务: " + task.target());

            // 纯计算：执行统计工具获取原始 JSON 结果
            String result = executeTask(table, task, state.dataProfile());
            System.out.println("分析结果: " + (result.length() > 200 ? result.substring(0, 200) + "..." : result));

            // AI 解读：将原始统计结果转化为中文叙述
            String narrative = interpretWithAI(task, result);
            System.out.println("AI叙述: " + (narrative.length() > 200 ? narrative.substring(0, 200) + "..." : narrative));

            Map<String, String> resultMap = new LinkedHashMap<>();
            resultMap.put("taskId", task.taskId());
            resultMap.put("type", task.type().name());
            resultMap.put("target", task.target());
            resultMap.put("result", result);
            resultMap.put("narrative", narrative);

            return Map.of(
                AnalysisState.ANALYSIS_RESULTS_KEY, resultMap,
                AnalysisState.ANALYSIS_PLAN_KEY, plan.advance(),
                AnalysisState.CURRENT_STEP_KEY, "ANALYZED",
                AnalysisState.DATA_SUMMARY_KEY, state.dataSummary() + "\n\n### " + task.target() + "\n" + narrative
            );
        } catch (Exception e) {
            System.err.println("数据分析失败: " + e.getMessage());
            Map<String, Object> output = new LinkedHashMap<>();
            output.put(AnalysisState.ERRORS_KEY, List.of("数据分析失败: " + e.getMessage()));
            output.put(AnalysisState.CURRENT_STEP_KEY, "ERROR");
            AnalysisPlan plan = state.analysisPlan();
            if (plan != null) {
                output.put(AnalysisState.ANALYSIS_PLAN_KEY, plan.advance());
            }
            return output;
        }
    }

    /**
     * 使用 AI 解读分析结果，生成中文叙述文本
     *
     * @param task       当前分析任务
     * @param jsonResult 原始 JSON 统计结果
     * @return AI 生成的中文叙述文本，失败时降级返回原始 JSON
     */
    private String interpretWithAI(AnalysisTask task, String jsonResult) {
        try {
            String prompt = """
                你是数据分析专家。请将以下分析结果转化为简洁易懂的中文叙述。

                分析类型：%s
                分析目标：%s
                原始数据：%s

                要求：
                1. 用2-3句话总结关键发现
                2. 提取最重要的数据指标（如均值、总量、排名等）
                3. 指出值得关注的趋势或异常
                4. 不要重复原始JSON，用自然语言描述
                5. 只输出叙述文本，不要其他内容
                """.formatted(task.type().name(), task.target(), jsonResult);

            return model.chat(prompt);
        } catch (Exception e) {
            System.err.println("AI解读失败，使用原始结果: " + e.getMessage());
            return jsonResult;
        }
    }

    String executeTask(Table table, AnalysisTask task, DataProfile profile) {
        var params = task.parameters();
        String dateCol = parameter(params, null, "date", "dateColumn", "date_column");
        String valueCol = parameter(params, null, "valueColumn", "value_column", "amount");
        String groupCol = parameter(params, null, "groupColumn", "group_column",
            "categoryColumn", "category_column", "region");
        String granularity = parameter(params, "month", "interval", "timeGranularity", "time_granularity");

        // profile 可用时，按列类型补齐缺失的列名
        if (profile != null) {
            if (dateCol == null) dateCol = ColumnSelection.firstDate(profile);
            if (valueCol == null) valueCol = ColumnSelection.firstNumeric(profile);
            if (groupCol == null) groupCol = ColumnSelection.firstCategorical(profile);
        }

        return switch (task.type()) {
            case TREND -> (dateCol == null || valueCol == null)
                ? "{\"error\": \"缺少日期列或数值列，无法做趋势分析\"}"
                : analysisTools.salesTrendAnalysis(table, dateCol, valueCol, granularity);
            case DISTRIBUTION -> analysisTools.descriptiveStats(table);
            case CORRELATION -> {
                String c1 = orElse(correlationColumn(params, 0, null, "col1"),
                    profile != null ? ColumnSelection.firstNumeric(profile) : null);
                String c2 = orElse(correlationColumn(params, 1, null, "col2"),
                    profile != null ? ColumnSelection.secondNumeric(profile) : null);
                yield (c1 == null || c2 == null)
                    ? "{\"error\": \"数值列不足，无法做相关性分析\"}"
                    : analysisTools.correlationAnalysis(table, c1, c2);
            }
            case COMPARISON -> (groupCol == null || valueCol == null)
                ? "{\"error\": \"缺少分类列或数值列，无法做对比分析\"}"
                : analysisTools.regionalSalesAnalysis(table, groupCol, valueCol);
            case OUTLIER -> {
                String c1 = orElse(params.get("col1"),
                    profile != null ? ColumnSelection.firstNumeric(profile) : null);
                String c2 = orElse(params.get("col2"),
                    profile != null ? ColumnSelection.secondNumeric(profile) : null);
                yield (c1 == null || c2 == null)
                    ? "{\"error\": \"数值列不足，无法做离群分析\"}"
                    : analysisTools.correlationAnalysis(table, c1, c2);
            }
            default -> "{\"note\": \"不支持的分析类型\"}";
        };
    }

    /** 返回第一个非空白值，全为空则返回 fallback。 */
    private static String orElse(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private static String parameter(Map<String, String> params, String fallback, String... keys) {
        for (String key : keys) {
            String value = params.get(key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return fallback;
    }

    private static String correlationColumn(
            Map<String, String> params, int index, String fallback, String directKey) {
        String direct = parameter(params, "", directKey);
        if (!direct.isBlank()) {
            return direct;
        }
        String[] columns = parameter(params, "", "columns").split(",");
        return index < columns.length && !columns[index].isBlank() ? columns[index].trim() : fallback;
    }
}
