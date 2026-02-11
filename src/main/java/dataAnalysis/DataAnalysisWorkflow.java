package dataAnalysis;

import dataAnalysis.agents.*;
import java.util.List;
import java.util.Map;

/**
 * Data Analysis Workflow Orchestration
 * Simplified version - executes agents sequentially
 */
public class DataAnalysisWorkflow {

    private final CSVParserAgent csvParserAgent;
    private final DataCleanerAgent dataCleanerAgent;
    private final DataAnalyzerAgent dataAnalyzerAgent;
    private final ChartGeneratorAgent chartGeneratorAgent;
    private final ReportGeneratorAgent reportGeneratorAgent;

    public DataAnalysisWorkflow() {
        this.csvParserAgent = new CSVParserAgent();
        this.dataCleanerAgent = new DataCleanerAgent();
        this.dataAnalyzerAgent = new DataAnalyzerAgent();
        this.chartGeneratorAgent = new ChartGeneratorAgent();
        this.reportGeneratorAgent = new ReportGeneratorAgent();
    }

    /**
     * Execute complete data analysis workflow
     */
    public AnalysisResult execute(String csvPath) {
        System.out.println("========================================");
        System.out.println("Data Analysis Multi-Agent System");
        System.out.println("========================================");

        // Prepare initial state
        Map<String, Object> currentState = Map.of(
                AnalysisState.CSV_PATH_KEY, csvPath,
                AnalysisState.CURRENT_STEP_KEY, "START"
        );

        try {
            // Step 1: CSV Parsing
            System.out.println("\n>>> Step 1: CSV File Parsing");
            Map<String, Object> parserResult = csvParserAgent.apply(createState(currentState));
            currentState = mergeState(currentState, parserResult);

            // Step 2: Data Cleaning
            System.out.println("\n>>> Step 2: Data Cleaning");
            Map<String, Object> cleanerResult = dataCleanerAgent.apply(createState(currentState));
            currentState = mergeState(currentState, cleanerResult);

            // Step 3: Data Analysis
            System.out.println("\n>>> Step 3: Data Analysis");
            Map<String, Object> analyzerResult = dataAnalyzerAgent.apply(createState(currentState));
            currentState = mergeState(currentState, analyzerResult);

            // Step 4: Chart Generation
            System.out.println("\n>>> Step 4: Chart Generation");
            Map<String, Object> chartResult = chartGeneratorAgent.apply(createState(currentState));
            currentState = mergeState(currentState, chartResult);

            // Step 5: Report Generation
            System.out.println("\n>>> Step 5: Report Generation");
            Map<String, Object> reportResult = reportGeneratorAgent.apply(createState(currentState));
            currentState = mergeState(currentState, reportResult);

            System.out.println("\n========================================");
            System.out.println("Workflow Execution Complete");
            System.out.println("========================================");

            return new AnalysisResult(
                    csvPath,
                    (String) currentState.get(AnalysisState.REPORT_PATH_KEY),
                    (List<String>) currentState.getOrDefault(AnalysisState.CHART_PATHS_KEY, List.of()),
                    (Map<String, String>) currentState.getOrDefault(AnalysisState.ANALYSIS_RESULT_KEY, Map.of()),
                    (String) currentState.getOrDefault(AnalysisState.DATA_SUMMARY_KEY, "")
            );

        } catch (Exception e) {
            System.err.println("Workflow execution failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Data analysis workflow execution failed", e);
        }
    }

    /**
     * Create state object
     */
    private AnalysisState createState(Map<String, Object> data) {
        return new AnalysisState(data);
    }

    /**
     * Merge state updates
     */
    private Map<String, Object> mergeState(Map<String, Object> current, Map<String, Object> update) {
        Map<String, Object> merged = new java.util.HashMap<>(current);
        merged.putAll(update);
        return merged;
    }

    /**
     * Analysis Result Data Class
     */
    public static class AnalysisResult {
        private final String csvPath;
        private final String reportPath;
        private final List<String> chartPaths;
        private final Map<String, String> analysisResult;
        private final String summary;

        public AnalysisResult(String csvPath, String reportPath,
                           List<String> chartPaths,
                           Map<String, String> analysisResult,
                           String summary) {
            this.csvPath = csvPath;
            this.reportPath = reportPath;
            this.chartPaths = chartPaths != null ? chartPaths : List.of();
            this.analysisResult = analysisResult != null ? analysisResult : Map.of();
            this.summary = summary != null ? summary : "";
        }

        public String getCsvPath() {
            return csvPath;
        }

        public String getReportPath() {
            return reportPath;
        }

        public List<String> getChartPaths() {
            return chartPaths;
        }

        public Map<String, String> getAnalysisResult() {
            return analysisResult;
        }

        public String getSummary() {
            return summary;
        }

        @Override
        public String toString() {
            return String.format(
                    "========================================%n" +
                    "Data Analysis Results%n" +
                    "========================================%n" +
                    "CSV File: %s%n" +
                    "Report Path: %s%n" +
                    "Chart Count: %d%n" +
                    "========================================",
                    csvPath, reportPath, chartPaths.size());
        }
    }
}
