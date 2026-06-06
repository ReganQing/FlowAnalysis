package dataAnalysis;

import dataAnalysis.nodes.*;
import dataAnalysis.model.AnalysisPlan;
import dataAnalysis.router.IntelliModelRouter;
import dataAnalysis.router.ModelRouter;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncCommandAction;
import org.bsc.langgraph4j.action.AsyncEdgeAction;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

import java.util.Map;

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
        return new StateGraph<>(AnalysisState.SCHEMA, AnalysisState::new)
            .addNode("parser",   node_async(new CSVParserNode()))
            .addNode("cleaner",  node_async(new DataCleanerNode()))
            .addNode("planner",  node_async(new AIPlannerNode(modelRouter)))
            .addNode("analyzer", node_async(new DataAnalyzerNode(modelRouter)))
            .addNode("insight",  node_async(new InsightNode(modelRouter)))
            .addNode("chart",    node_async(new ChartGeneratorNode()))
            .addNode("report",   node_async(new ReportGeneratorNode(modelRouter)))
            .addEdge(START,      "parser")
            .addEdge("parser",   "cleaner")
            .addEdge("cleaner",  "planner")
            .addConditionalEdges("planner",
                    AsyncCommandAction.of(AsyncEdgeAction.edge_async(this::routeAnalysis)),
                    Map.of("analyzer", "analyzer", "insight", "insight", "chart", "chart"))
            .addEdge("analyzer", "chart")
            .addEdge("insight",  "chart")
            .addEdge("chart",    "report")
            .addEdge("report",   END)
            .compile();
    }

    private String routeAnalysis(AnalysisState state) {
        AnalysisPlan plan = state.analysisPlan();
        if (plan == null || !plan.hasMoreTasks()) {
            return "chart";
        }
        return plan.nextStep();
    }

    public AnalysisResult execute(String csvPath) throws Exception {
        CompiledGraph<AnalysisState> graph = buildGraph();

        System.out.println("========================================");
        System.out.println("数据分析多智能体系统");
        System.out.println("基于 LangGraph4J + LangChain4j");
        System.out.println("========================================\n");

        AnalysisState finalState = null;
        for (var nodeOutput : graph.stream(Map.of(
                AnalysisState.CSV_PATH_KEY, csvPath,
                AnalysisState.CURRENT_STEP_KEY, "START"
        ))) {
            finalState = nodeOutput.state();
            System.out.println("[完成] 节点: " + nodeOutput.node());
        }

        if (finalState == null) {
            throw new RuntimeException("工作流执行失败：未产生最终状态");
        }

        return new AnalysisResult(
            csvPath,
            finalState.reportPath(),
            finalState.chartEmbeds().stream().map(c -> c.title()).toList(),
            finalState.dataSummary(),
            finalState.errors()
        );
    }

    public static class AnalysisResult {
        private final String csvPath;
        private final String reportPath;
        private final java.util.List<String> chartTitles;
        private final String summary;
        private final java.util.List<String> errors;

        public AnalysisResult(String csvPath, String reportPath,
                              java.util.List<String> chartTitles,
                              String summary, java.util.List<String> errors) {
            this.csvPath = csvPath;
            this.reportPath = reportPath;
            this.chartTitles = chartTitles;
            this.summary = summary;
            this.errors = errors;
        }

        public String getCsvPath() { return csvPath; }
        public String getReportPath() { return reportPath; }
        public java.util.List<String> getChartTitles() { return chartTitles; }
        public String getSummary() { return summary; }
        public java.util.List<String> getErrors() { return errors; }

        @Override
        public String toString() {
            return String.format(
                "=== 分析结果 ===%n数据源: %s%n报告: %s%n图表: %d个%n错误: %d个",
                csvPath, reportPath, chartTitles.size(), errors.size());
        }
    }
}
