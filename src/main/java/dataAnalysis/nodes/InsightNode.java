package dataAnalysis.nodes;

import dataAnalysis.AnalysisState;
import dataAnalysis.model.Insight;
import dataAnalysis.model.InsightSeverity;
import dataAnalysis.router.ModelRouter;
import dev.langchain4j.model.chat.ChatModel;
import org.bsc.langgraph4j.action.NodeAction;

import java.util.*;

/**
 * 智能洞察节点
 */
public class InsightNode implements NodeAction<AnalysisState> {

    private final ChatModel model;

    public InsightNode(ModelRouter modelRouter) {
        this.model = modelRouter.getModelForTask(ModelRouter.TaskType.INSIGHT);
    }

    @Override
    public Map<String, Object> apply(AnalysisState state) {
        System.out.println("=== [InsightNode] 开始执行 ===");

        try {
            String summary = state.dataSummary();
            if (summary == null || summary.isEmpty()) {
                return Map.of(AnalysisState.CURRENT_STEP_KEY, "INSIGHT_SKIPPED");
            }

            String prompt = """
                你是一个数据分析专家。请根据以下分析结果，提取3-5条关键洞察。

                %s

                请输出JSON数组格式的洞察列表：
                [
                  {
                    "title": "洞察标题（简洁有力）",
                    "severity": "CRITICAL|WARNING|INFO|POSITIVE",
                    "evidence": "支撑数据",
                    "explanation": "可能的解释",
                    "recommendation": "行动建议"
                  }
                ]

                规则：
                1. 每条洞察必须有具体数据支撑
                2. severity基于业务影响判断
                3. 建议要具体可操作
                4. 只输出JSON数组，不要其他内容
                """.formatted(summary);

            String response = model.chat(prompt);
            System.out.println("AI洞察响应: " + response);

            List<Insight> insights = parseInsights(response);
            System.out.println("提取到 " + insights.size() + " 条洞察");

            return Map.of(
                AnalysisState.INSIGHTS_KEY, insights,
                AnalysisState.CURRENT_STEP_KEY, "INSIGHT_GENERATED"
            );
        } catch (Exception e) {
            System.err.println("洞察生成失败: " + e.getMessage());
            return Map.of(
                AnalysisState.ERRORS_KEY, List.of("洞察生成失败: " + e.getMessage()),
                AnalysisState.CURRENT_STEP_KEY, "INSIGHT_ERROR"
            );
        }
    }

    private List<Insight> parseInsights(String response) {
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
            var insights = new ArrayList<Insight>();

            if (tree.isArray()) {
                for (var item : tree) {
                    insights.add(new Insight(
                        item.get("title").asText("未命名洞察"),
                        InsightSeverity.valueOf(item.get("severity").asText("INFO")),
                        item.get("evidence").asText(""),
                        item.get("explanation").asText(""),
                        item.get("recommendation").asText("")
                    ));
                }
            }
            return insights;
        } catch (Exception e) {
            System.err.println("解析洞察JSON失败: " + e.getMessage());
            return List.of();
        }
    }
}
