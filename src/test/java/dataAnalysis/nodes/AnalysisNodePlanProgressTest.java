package dataAnalysis.nodes;

import dataAnalysis.AnalysisState;
import dataAnalysis.ProgressListener;
import dataAnalysis.model.AnalysisPlan;
import dataAnalysis.model.AnalysisTask;
import dataAnalysis.model.AnalysisType;
import dataAnalysis.model.SubTaskEvent;
import dataAnalysis.router.ModelRouter;
import dataAnalysis.DataAnalysisGraph;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void analyzerEmitsStartedThenCompletedWithCorrectIndexAndTotal() throws Exception {
        Path csv = tempDir.resolve("sales.csv");
        Files.writeString(csv, "date,quantity,amount\n2026-01-01,1,10\n2026-01-02,2,20\n");
        // 两个 ANALYSIS 任务：先 TREND，后 DISTRIBUTION
        AnalysisPlan plan = new AnalysisPlan(
            List.of(
                new AnalysisTask("t1", AnalysisType.TREND, "趋势分析", Map.of(), 1),
                new AnalysisTask("t2", AnalysisType.DISTRIBUTION, "分布统计", Map.of(), 1)
            ),
            "reasoning", List.of(), 0);
        AnalysisState state = new AnalysisState(Map.of(
            AnalysisState.CSV_PATH_KEY, csv.toString(),
            AnalysisState.ANALYSIS_PLAN_KEY, plan
        ));

        CapturingListener listener = new CapturingListener();
        new DataAnalyzerNode(new FakeRouter(), listener).apply(state);

        assertEquals(2, listener.subTasks.size());
        // 处理第一个 ANALYSIS 任务：STARTED -> COMPLETED，均为 index=1 total=2
        assertSubTask(listener.subTasks.get(0), 1, 2, "趋势分析", SubTaskEvent.TaskStatus.STARTED);
        assertSubTask(listener.subTasks.get(1), 1, 2, "趋势分析", SubTaskEvent.TaskStatus.COMPLETED);
    }

    @Test
    void insightEmitsStartedThenCompletedWithInsightScopeIndex() {
        // 1 个 INSIGHT 任务 + 1 个 ANALYSIS 任务：INSIGHT scope 内仅 1 个，total=1 index=1
        AnalysisPlan plan = new AnalysisPlan(
            List.of(
                new AnalysisTask("a1", AnalysisType.DISTRIBUTION, "分布统计", Map.of(), 1),
                new AnalysisTask("i1", AnalysisType.INSIGHT, "深度洞察", Map.of(), 1)
            ),
            "reasoning", List.of(), 1); // currentTaskIndex=1，指向 INSIGHT 任务
        AnalysisState state = new AnalysisState(Map.of(
            AnalysisState.DATA_SUMMARY_KEY, "summary",
            AnalysisState.ANALYSIS_PLAN_KEY, plan
        ));

        CapturingListener listener = new CapturingListener();
        new InsightNode(new FakeRouter(), listener).apply(state);

        assertEquals(2, listener.subTasks.size());
        assertSubTask(listener.subTasks.get(0), 1, 1, "深度洞察", SubTaskEvent.TaskStatus.STARTED);
        assertSubTask(listener.subTasks.get(1), 1, 1, "深度洞察", SubTaskEvent.TaskStatus.COMPLETED);
    }

    @Test
    void insightWithEmptySummaryEmitsNoSubTaskEvents() {
        // 默认 dataSummary 为 ""，跳过路径不应发出任何 STARTED/COMPLETED/ERROR
        AnalysisPlan plan = new AnalysisPlan(
            List.of(new AnalysisTask("i1", AnalysisType.INSIGHT, "深度洞察", Map.of(), 1)),
            "reasoning", List.of(), 0);
        AnalysisState state = new AnalysisState(Map.of(
            AnalysisState.ANALYSIS_PLAN_KEY, plan
        ));

        CapturingListener listener = new CapturingListener();
        Map<String, Object> output = new InsightNode(new FakeRouter(), listener).apply(state);

        assertTrue(listener.subTasks.isEmpty(), "跳过路径不应发出任何子任务事件");
        assertEquals("INSIGHT_SKIPPED", output.get(AnalysisState.CURRENT_STEP_KEY));
    }

    private static void assertSubTask(SubTaskEvent event,
                                      int expectedIndex, int expectedTotal,
                                      String expectedLabel, SubTaskEvent.TaskStatus expectedStatus) {
        assertEquals(expectedIndex, event.index(), "index mismatch");
        assertEquals(expectedTotal, event.total(), "total mismatch");
        assertEquals(expectedLabel, event.label(), "label mismatch");
        assertEquals(expectedStatus, event.status(), "status mismatch");
    }

    /** 记录 onSubTask 事件的捕获监听器，其余回调全部空操作。 */
    private static final class CapturingListener implements ProgressListener {
        final List<SubTaskEvent> subTasks = new ArrayList<>();

        @Override
        public void onSubTask(String nodeName, SubTaskEvent event) {
            subTasks.add(event);
        }

        @Override public void onNodeStart(String nodeName, int stageIndex) { }
        @Override public void onNodeProgress(String nodeName, String message) { }
        @Override public void onNodeComplete(String nodeName, long durationMs) { }
        @Override public void onNodeError(String nodeName, String error) { }
        @Override public void onPipelineComplete(DataAnalysisGraph.AnalysisResult result) { }
        @Override public void onPipelineError(String error) { }
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
