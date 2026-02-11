package dataAnalysis.agents;

import dataAnalysis.AnalysisState;
import dataAnalysis.tools.CSVTools;
import dev.langchain4j.agent.tool.Tool;

import java.util.List;
import java.util.Map;

/**
 * CSV 文件解析智能体
 * 负责读取和解析 CSV 文件
 */
public class CSVParserAgent {

    private final CSVTools csvTools;

    public CSVParserAgent() {
        this.csvTools = new CSVTools();
    }

    public Map<String, Object> apply(AnalysisState state) {
        System.out.println("=== CSVParserAgent 开始执行 ===");

        String csvPath = state.csvPath();
        System.out.println("解析文件: " + csvPath);

        try {
            // 使用工具解析 CSV
            String parseResult = csvTools.parseCSV(csvPath);
            System.out.println("解析结果: " + parseResult);

            // 获取统计信息
            String stats = csvTools.getCSVStatistics(csvPath);
            System.out.println("文件统计: " + stats);

            // 获取列名
            String columns = csvTools.getColumnNames(csvPath);
            System.out.println("列名: " + columns);

            return Map.of(
                    AnalysisState.RAW_DATA_KEY, Map.of(
                            "csv_path", csvPath,
                            "parse_result", parseResult,
                            "statistics", stats,
                            "columns", columns
                    ),
                    AnalysisState.CURRENT_STEP_KEY, "CSV_PARSED",
                    AnalysisState.DATA_SUMMARY_KEY, String.format(
                            "文件路径: %s\n\n解析结果: %s\n\n统计信息: %s\n\n列名: %s",
                            csvPath, parseResult, stats, columns
                    )
            );

        } catch (Exception e) {
            System.err.println("CSV 解析失败: " + e.getMessage());
            return Map.of(
                    AnalysisState.ERRORS_KEY, List.of("CSV 解析失败: " + e.getMessage()),
                    AnalysisState.CURRENT_STEP_KEY, "ERROR"
            );
        }
    }
}
