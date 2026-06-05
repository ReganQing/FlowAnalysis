package dataAnalysis.nodes;

import dataAnalysis.AnalysisState;
import dataAnalysis.model.*;
import dataAnalysis.router.ModelRouter;
import dev.langchain4j.model.chat.ChatModel;
import org.bsc.langgraph4j.action.NodeAction;

import java.util.*;

/**
 * AI分析规划节点
 * LLM审视数据概况后自主决定分析维度
 */
public class AIPlannerNode implements NodeAction<AnalysisState> {

    private final ChatModel model;

    public AIPlannerNode(ModelRouter modelRouter) {
        this.model = modelRouter.getModelForTask(ModelRouter.TaskType.PLANNING);
    }

    @Override
    public Map<String, Object> apply(AnalysisState state) {
        System.out.println("=== [AIPlannerNode] 开始执行 ===");

        try {
            DataProfile profile = state.dataProfile();
            if (profile == null) {
                return Map.of(
                    AnalysisState.ERRORS_KEY, List.of("无数据概况，无法规划分析"),
                    AnalysisState.CURRENT_STEP_KEY, "ERROR"
                );
            }

            String prompt = buildPlanningPrompt(profile);
            System.out.println("发送规划请求给AI...");

            String response = model.chat(prompt);
            System.out.println("AI规划响应: " + response);

            AnalysisPlan plan = parsePlanFromResponse(response, profile);

            System.out.println("分析计划: " + plan.reasoning());
            System.out.println("规划任务数: " + plan.tasks().size());

            return Map.of(
                AnalysisState.ANALYSIS_PLAN_KEY, plan,
                AnalysisState.CURRENT_STEP_KEY, "PLANNED",
                AnalysisState.DATA_SUMMARY_KEY, state.dataSummary() + "\n\n## 分析规划\n" + plan.reasoning()
            );
        } catch (Exception e) {
            System.err.println("AI规划失败: " + e.getMessage());
            AnalysisPlan fallbackPlan = createFallbackPlan(state.dataProfile());
            System.out.println("降级为默认分析计划，任务数: " + fallbackPlan.tasks().size());

            return Map.of(
                AnalysisState.ANALYSIS_PLAN_KEY, fallbackPlan,
                AnalysisState.CURRENT_STEP_KEY, "PLANNED",
                AnalysisState.DATA_SUMMARY_KEY, state.dataSummary() + "\n\n## 分析规划（降级默认）"
            );
        }
    }

    private String buildPlanningPrompt(DataProfile profile) {
        return """
            你是一个数据分析专家。请根据以下数据概况，规划最适合的分析任务。

            %s

            请输出一个JSON格式的分析计划，包含以下字段：
            {
              "reasoning": "你的分析规划理由",
              "tasks": [
                {
                  "taskId": "task_1",
                  "type": "TREND|DISTRIBUTION|CORRELATION|COMPARISON|OUTLIER|INSIGHT",
                  "target": "分析目标描述",
                  "parameters": {"key": "value"},
                  "priority": 1
                }
              ],
              "suggestedCharts": ["bar", "line", "pie"]
            }

            规则：
            1. 根据数据特征选择最有价值的分析类型
            2. 如果有日期列和数值列，建议TREND分析
            3. 如果有分类列和数值列，建议COMPARISON分析
            4. 如果有多个数值列，建议CORRELATION分析
            5. 最后添加一个INSIGHT任务用于AI洞察
            6. 优先级1最高，数字越大越低
            7. 只输出JSON，不要其他内容
            """.formatted(profile.toSummary());
    }

    private AnalysisPlan parsePlanFromResponse(String response, DataProfile profile) {
        try {
            String json = response;
            if (response.contains("```json")) {
                json = response.substring(response.indexOf("```json") + 7);
                json = json.substring(0, json.indexOf("```"));
            } else if (response.contains("```")) {
                json = response.substring(response.indexOf("```") + 3);
                json = json.substring(0, json.indexOf("```"));
            }
            json = json.trim();

            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var tree = mapper.readTree(json);

            var tasks = new ArrayList<AnalysisTask>();
            var tasksNode = tree.get("tasks");
            if (tasksNode != null && tasksNode.isArray()) {
                for (var taskNode : tasksNode) {
                    var params = new LinkedHashMap<String, String>();
                    var paramsNode = taskNode.get("parameters");
                    if (paramsNode != null && paramsNode.isObject()) {
                        paramsNode.fields().forEachRemaining(e -> params.put(e.getKey(), e.getValue().asText()));
                    }

                    tasks.add(new AnalysisTask(
                        taskNode.get("taskId").asText("task_" + (tasks.size() + 1)),
                        AnalysisType.valueOf(taskNode.get("type").asText("INSIGHT")),
                        taskNode.get("target").asText(""),
                        params,
                        taskNode.get("priority").asInt(99)
                    ));
                }
            }

            var charts = new ArrayList<String>();
            var chartsNode = tree.get("suggestedCharts");
            if (chartsNode != null && chartsNode.isArray()) {
                for (var chartNode : chartsNode) {
                    charts.add(chartNode.asText());
                }
            }

            return new AnalysisPlan(tasks,
                tree.get("reasoning").asText("AI规划"), charts, 0);
        } catch (Exception e) {
            System.err.println("解析AI规划响应失败: " + e.getMessage());
            return createFallbackPlan(profile);
        }
    }

    private AnalysisPlan createFallbackPlan(DataProfile profile) {
        var tasks = new ArrayList<AnalysisTask>();

        if (!profile.dateColumns().isEmpty() && !profile.numericColumns().isEmpty()) {
            tasks.add(new AnalysisTask("task_trend", AnalysisType.TREND,
                "时间趋势分析",
                Map.of("dateColumn", profile.dateColumns().get(0),
                       "valueColumn", profile.numericColumns().get(0),
                       "interval", "month"),
                1));
        }

        if (!profile.categoricalColumns().isEmpty() && !profile.numericColumns().isEmpty()) {
            tasks.add(new AnalysisTask("task_compare", AnalysisType.COMPARISON,
                "分类对比分析",
                Map.of("groupColumn", profile.categoricalColumns().get(0),
                       "valueColumn", profile.numericColumns().get(0)),
                2));
        }

        if (profile.numericColumns().size() >= 2) {
            tasks.add(new AnalysisTask("task_corr", AnalysisType.CORRELATION,
                "相关性分析",
                Map.of("col1", profile.numericColumns().get(0),
                       "col2", profile.numericColumns().get(1)),
                3));
        }

        tasks.add(new AnalysisTask("task_insight", AnalysisType.INSIGHT,
            "AI智能洞察", Map.of(), 4));

        return new AnalysisPlan(tasks, "降级默认分析计划",
            List.of("bar", "line", "pie"), 0);
    }
}
