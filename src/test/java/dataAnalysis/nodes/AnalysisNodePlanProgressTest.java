package dataAnalysis.nodes;

import dataAnalysis.AnalysisState;
import dataAnalysis.model.AnalysisPlan;
import dataAnalysis.model.AnalysisTask;
import dataAnalysis.model.AnalysisType;
import dataAnalysis.router.ModelRouter;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnalysisNodePlanProgressTest {

    @TempDir
    Path tempDir;

    @Test
    void analyzerAdvancesToTheNextPlannedTask() throws Exception {
        Path csv = tempDir.resolve("sales.csv");
        Files.writeString(csv, "date,quantity,amount\n2026-01-01,1,10\n2026-01-02,2,20\n");
        AnalysisPlan plan = planWith(AnalysisType.TREND);
        AnalysisState state = new AnalysisState(Map.of(
            AnalysisState.CSV_PATH_KEY, csv.toString(),
            AnalysisState.ANALYSIS_PLAN_KEY, plan
        ));

        Map<String, Object> output = new DataAnalyzerNode(new FakeRouter()).apply(state);

        assertEquals(1, ((AnalysisPlan) output.get(AnalysisState.ANALYSIS_PLAN_KEY)).currentTaskIndex());
    }

    @Test
    void analyzerHonorsSnakeCaseParametersReturnedByPlanner() throws Exception {
        Path csv = tempDir.resolve("products.csv");
        Files.writeString(csv, "region,product_name,amount\nNorth,Laptop,10\nSouth,Mouse,20\n");
        AnalysisPlan plan = new AnalysisPlan(
            List.of(new AnalysisTask("task", AnalysisType.COMPARISON, "target",
                Map.of("category_column", "product_name", "value_column", "amount"), 1)),
            "reasoning", List.of(), 0);
        AnalysisState state = new AnalysisState(Map.of(
            AnalysisState.CSV_PATH_KEY, csv.toString(),
            AnalysisState.ANALYSIS_PLAN_KEY, plan
        ));

        Map<String, Object> output = new DataAnalyzerNode(new FakeRouter()).apply(state);
        @SuppressWarnings("unchecked")
        String result = ((Map<String, String>) output.get(AnalysisState.ANALYSIS_RESULTS_KEY)).get("result");

        org.junit.jupiter.api.Assertions.assertTrue(result.contains("Laptop"));
        org.junit.jupiter.api.Assertions.assertTrue(result.contains("Mouse"));
    }

    @Test
    void insightAdvancesToTheNextPlannedTask() {
        AnalysisPlan plan = planWith(AnalysisType.INSIGHT);
        AnalysisState state = new AnalysisState(Map.of(
            AnalysisState.DATA_SUMMARY_KEY, "summary",
            AnalysisState.ANALYSIS_PLAN_KEY, plan
        ));

        Map<String, Object> output = new InsightNode(new FakeRouter()).apply(state);

        assertEquals(1, ((AnalysisPlan) output.get(AnalysisState.ANALYSIS_PLAN_KEY)).currentTaskIndex());
    }

    private static AnalysisPlan planWith(AnalysisType type) {
        return new AnalysisPlan(
            List.of(new AnalysisTask("task", type, "target", Map.of(), 1)),
            "reasoning", List.of(), 0);
    }

    private static class FakeRouter implements ModelRouter {
        private final ChatModel model = new ChatModel() {
            @Override
            public String chat(String prompt) {
                return prompt.contains("JSON数组") ? "[]" : "narrative";
            }
        };

        @Override
        public ChatModel getModelForTask(TaskType taskType) {
            return model;
        }

        @Override
        public StreamingChatModel getStreamingModelForTask(TaskType taskType) {
            return null;
        }
    }
}
