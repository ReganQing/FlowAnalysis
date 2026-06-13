package dataAnalysis;

import dataAnalysis.nodes.*;
import dataAnalysis.model.AnalysisPlan;
import dataAnalysis.router.IntelliModelRouter;
import dataAnalysis.router.ModelRouter;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncCommandAction;
import org.bsc.langgraph4j.action.AsyncEdgeAction;
import org.bsc.langgraph4j.action.NodeAction;

import java.util.List;
import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * LangGraph4J 状态图定义
 * 编排数据分析的完整工作流
 */
public class DataAnalysisGraph {

    private final ModelRouter modelRouter;

    public DataAnalysisGraph() {
        this(new IntelliModelRouter());
    }

    public DataAnalysisGraph(ModelRouter modelRouter) {
        this.modelRouter = modelRouter;
    }

    public CompiledGraph<AnalysisState> buildGraph() throws Exception {
        return buildGraph(null);
    }

    private CompiledGraph<AnalysisState> buildGraph(ProgressListener listener) throws Exception {
        return new StateGraph<>(AnalysisState.SCHEMA, AnalysisState::new)
            .addNode("parser",   tracked("parser", new CSVParserNode(), listener))
            .addNode("cleaner",  tracked("cleaner", new DataCleanerNode(), listener))
            .addNode("planner",  tracked("planner", new AIPlannerNode(modelRouter), listener))
            .addNode("analyzer", tracked("analyzer", new DataAnalyzerNode(modelRouter, listener), listener))
            .addNode("insight",  tracked("insight", new InsightNode(modelRouter, listener), listener))
            .addNode("chart",    tracked("chart", new ChartGeneratorNode(), listener))
            .addNode("report",   tracked("report", new ReportGeneratorNode(modelRouter), listener))
            .addEdge(START,      "parser")
            .addEdge("parser",   "cleaner")
            .addEdge("cleaner",  "planner")
            .addConditionalEdges("planner",
                    AsyncCommandAction.of(AsyncEdgeAction.edge_async(this::routeAnalysis)),
                    Map.of("analyzer", "analyzer", "insight", "insight", "chart", "chart"))
            .addConditionalEdges("analyzer",
                    AsyncCommandAction.of(AsyncEdgeAction.edge_async(this::routeAnalysis)),
                    Map.of("analyzer", "analyzer", "insight", "insight", "chart", "chart"))
            .addConditionalEdges("insight",
                    AsyncCommandAction.of(AsyncEdgeAction.edge_async(this::routeAnalysis)),
                    Map.of("analyzer", "analyzer", "insight", "insight", "chart", "chart"))
            .addEdge("chart",    "report")
            .addEdge("report",   END)
            .compile();
    }

    private static org.bsc.langgraph4j.action.AsyncNodeAction<AnalysisState> tracked(
            String nodeName, NodeAction<AnalysisState> action, ProgressListener listener) {
        return node_async(ProgressTrackingNodeAction.wrap(
            nodeName, STAGE_INDEX.get(nodeName), action, listener));
    }

    private String routeAnalysis(AnalysisState state) {
        AnalysisPlan plan = state.analysisPlan();
        if (plan == null || !plan.hasMoreTasks()) {
            return "chart";
        }
        return plan.nextStep();
    }

    /** 节点名称 → 阶段序号映射 */
    private static final Map<String, Integer> STAGE_INDEX = Map.of(
        "parser",   1,
        "cleaner",  2,
        "planner",  3,
        "analyzer", 4,
        "insight",  5,
        "chart",    6,
        "report",   7
    );

    /**
     * 执行数据分析管线（无监听器，向后兼容）。
     */
    public AnalysisResult execute(String csvPath) throws Exception {
        return execute(csvPath, null);
    }

    /**
     * 执行数据分析管线，通过 {@link ProgressListener} 报告进度。
     */
    public AnalysisResult execute(String csvPath, ProgressListener listener) throws Exception {
        CompiledGraph<AnalysisState> graph = buildGraph(listener);

        log(listener, "========================================");
        log(listener, "数据分析多智能体系统");
        log(listener, "基于 LangGraph4J + LangChain4j");
        log(listener, "========================================\n");

        AnalysisState finalState = null;
        for (var nodeOutput : graph.stream(Map.of(
                AnalysisState.CSV_PATH_KEY, csvPath,
                AnalysisState.CURRENT_STEP_KEY, "START"
        ))) {
            String nodeName = nodeOutput.node();
            finalState = nodeOutput.state();

            System.out.println("[完成] 节点: " + nodeName);
        }

        if (finalState == null) {
            String error = "工作流执行失败：未产生最终状态";
            if (listener != null) {
                listener.onPipelineError(error);
            }
            throw new RuntimeException(error);
        }

        AnalysisResult result;
        try {
            result = createResult(csvPath, finalState);
        } catch (IllegalStateException e) {
            if (listener != null) {
                listener.onPipelineError(e.getMessage());
            }
            throw e;
        }

        if (listener != null) {
            listener.onPipelineComplete(result);
        }

        return result;
    }

    static AnalysisResult createResult(String csvPath, AnalysisState finalState) {
        if (finalState.reportPath().isBlank()) {
            String details = finalState.errors().isEmpty()
                ? "未生成报告文件"
                : String.join("; ", finalState.errors());
            throw new IllegalStateException("数据分析管线失败: " + details);
        }
        return new AnalysisResult(
            csvPath,
            finalState.reportPath(),
            finalState.chartEmbeds().stream().map(c -> c.title()).toList(),
            finalState.dataSummary(),
            finalState.errors(),
            collectOutputFiles(finalState)
        );
    }

    private void log(ProgressListener listener, String message) {
        if (listener == null) {
            System.out.println(message);
        }
    }

    /** 收集管线生成的所有输出文件路径（报告 + 图表）。 */
    private static List<String> collectOutputFiles(AnalysisState state) {
        java.util.List<String> files = new java.util.ArrayList<>();
        if (!state.reportPath().isBlank()) {
            files.add(state.reportPath());
        }
        state.chartEmbeds().stream()
            .map(dataAnalysis.model.ChartEmbed::outputPath)
            .filter(path -> path != null && !path.isBlank())
            .forEach(files::add);
        return files;
    }

    public static class AnalysisResult {
        private final String csvPath;
        private final String reportPath;
        private final java.util.List<String> chartTitles;
        private final String summary;
        private final java.util.List<String> errors;
        private final java.util.List<String> outputFiles;

        public AnalysisResult(String csvPath, String reportPath,
                              java.util.List<String> chartTitles,
                              String summary, java.util.List<String> errors) {
            this(csvPath, reportPath, chartTitles, summary, errors, java.util.List.of());
        }

        public AnalysisResult(String csvPath, String reportPath,
                              java.util.List<String> chartTitles,
                              String summary, java.util.List<String> errors,
                              java.util.List<String> outputFiles) {
            this.csvPath = csvPath;
            this.reportPath = reportPath;
            this.chartTitles = chartTitles;
            this.summary = summary;
            this.errors = errors;
            this.outputFiles = outputFiles;
        }

        public String getCsvPath() { return csvPath; }
        public String getReportPath() { return reportPath; }
        public java.util.List<String> getChartTitles() { return chartTitles; }
        public String getSummary() { return summary; }
        public java.util.List<String> getErrors() { return errors; }
        public java.util.List<String> getOutputFiles() { return outputFiles; }

        @Override
        public String toString() {
            return String.format(
                "=== 分析结果 ===%n数据源: %s%n报告: %s%n图表: %d个%n错误: %d个",
                csvPath, reportPath, chartTitles.size(), errors.size());
        }
    }
}
