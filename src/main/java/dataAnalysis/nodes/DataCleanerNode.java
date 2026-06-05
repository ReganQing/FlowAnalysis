package dataAnalysis.nodes;

import dataAnalysis.AnalysisState;
import dataAnalysis.tools.BaseTools;
import dataAnalysis.tools.DataCleaningTools;
import org.bsc.langgraph4j.action.NodeAction;
import tech.tablesaw.api.Table;

import java.util.List;
import java.util.Map;

/**
 * 数据清洗节点
 */
public class DataCleanerNode implements NodeAction<AnalysisState> {

    private final DataCleaningTools cleaningTools = new DataCleaningTools();

    @Override
    public Map<String, Object> apply(AnalysisState state) {
        System.out.println("=== [DataCleanerNode] 开始执行 ===");

        try {
            String csvPath = state.csvPath();
            Table table = BaseTools.loadCSVTable(csvPath);
            int originalRows = table.rowCount();
            System.out.println("原始行数: " + originalRows);

            String qualityReport = cleaningTools.getDataQualityReport(table);
            Table cleanedTable = cleaningTools.handleMissingValues(table, "mean", "");

            int beforeDedup = cleanedTable.rowCount();
            cleanedTable = cleaningTools.removeDuplicates(cleanedTable);
            int duplicatesRemoved = beforeDedup - cleanedTable.rowCount();

            String cleanedQualityReport = cleaningTools.getDataQualityReport(cleanedTable);

            String summary = String.format(
                "数据清洗完成: 原始%d行 → 清洗后%d行 (去重%d行)\n\n清洗后质量:\n%s",
                originalRows, cleanedTable.rowCount(), duplicatesRemoved, cleanedQualityReport
            );
            System.out.println(summary);

            return Map.of(
                AnalysisState.CLEANED_DATA_KEY, Map.of(
                    "rows", cleanedTable.rowCount(),
                    "columns", cleanedTable.columnCount(),
                    "quality_report", cleanedQualityReport,
                    "duplicates_removed", duplicatesRemoved
                ),
                AnalysisState.CURRENT_STEP_KEY, "DATA_CLEANED",
                AnalysisState.DATA_SUMMARY_KEY, state.dataSummary() + "\n\n" + summary
            );
        } catch (Exception e) {
            System.err.println("数据清洗失败: " + e.getMessage());
            return Map.of(
                AnalysisState.ERRORS_KEY, List.of("数据清洗失败: " + e.getMessage()),
                AnalysisState.CURRENT_STEP_KEY, "ERROR"
            );
        }
    }
}
