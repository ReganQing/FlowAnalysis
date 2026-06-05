package dataAnalysis.nodes;

import dataAnalysis.AnalysisState;
import dataAnalysis.processing.DataProfiler;
import dataAnalysis.tools.CSVTools;
import dataAnalysis.tools.BaseTools;
import org.bsc.langgraph4j.action.NodeAction;
import tech.tablesaw.api.Table;

import java.util.List;
import java.util.Map;

/**
 * CSV解析节点
 */
public class CSVParserNode implements NodeAction<AnalysisState> {

    private final CSVTools csvTools = new CSVTools();
    private final DataProfiler profiler = new DataProfiler();

    @Override
    public Map<String, Object> apply(AnalysisState state) {
        System.out.println("=== [CSVParserNode] 开始执行 ===");

        String csvPath = state.csvPath();
        System.out.println("解析文件: " + csvPath);

        try {
            String parseResult = csvTools.parseCSV(csvPath);
            Table table = BaseTools.loadCSVTable(csvPath);
            var profile = profiler.profile(table);

            String stats = csvTools.getCSVStatistics(csvPath);
            String columns = csvTools.getColumnNames(csvPath);

            String summary = String.format(
                "文件: %s (%d行 × %d列)\n\n列: %s\n\n统计: %s\n\n%s",
                csvPath, profile.rowCount(), profile.columnCount(), columns, stats, profile.toSummary()
            );

            System.out.println("数据概况: " + profile.rowCount() + "行 × " + profile.columnCount() + "列");

            return Map.of(
                AnalysisState.RAW_DATA_KEY, Map.of(
                    "csv_path", csvPath,
                    "parse_result", parseResult,
                    "statistics", stats,
                    "columns", columns
                ),
                AnalysisState.DATA_PROFILE_KEY, profile,
                AnalysisState.CURRENT_STEP_KEY, "CSV_PARSED",
                AnalysisState.DATA_SUMMARY_KEY, summary
            );
        } catch (Exception e) {
            System.err.println("CSV解析失败: " + e.getMessage());
            return Map.of(
                AnalysisState.ERRORS_KEY, List.of("CSV解析失败: " + e.getMessage()),
                AnalysisState.CURRENT_STEP_KEY, "ERROR"
            );
        }
    }
}
