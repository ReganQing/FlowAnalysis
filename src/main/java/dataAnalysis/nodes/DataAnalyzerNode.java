package dataAnalysis.nodes;

import dataAnalysis.AnalysisState;
import dataAnalysis.model.AnalysisPlan;
import dataAnalysis.model.AnalysisTask;
import dataAnalysis.model.AnalysisType;
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

            // 纯计算：执行统计工具获取原始 JSON 结果
            String result = executeTask(table, task);
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
                AnalysisState.CURRENT_STEP_KEY, "ANALYZED",
                AnalysisState.DATA_SUMMARY_KEY, state.dataSummary() + "\n\n### " + task.target() + "\n" + narrative
            );
        } catch (Exception e) {
            System.err.println("数据分析失败: " + e.getMessage());
            return Map.of(
                AnalysisState.ERRORS_KEY, List.of("数据分析失败: " + e.getMessage()),
                AnalysisState.CURRENT_STEP_KEY, "ERROR"
            );
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
