package dataAnalysis.agents;

import dataAnalysis.AnalysisState;
import dataAnalysis.tools.DataCleaningTools;
import dataAnalysis.tools.BaseTools;

import java.util.List;
import java.util.Map;

/**
 * 数据清洗智能体
 * 负责数据质量检查和清洗
 */
public class DataCleanerAgent {

    private final DataCleaningTools cleaningTools;

    public DataCleanerAgent() {
        this.cleaningTools = new DataCleaningTools();
    }

    public Map<String, Object> apply(AnalysisState state) {
        System.out.println("=== DataCleanerAgent 开始执行 ===");

        try {
            // 从状态获取 CSV 路径
            String csvPath = state.csvPath();
            System.out.println("开始清洗数据: " + csvPath);

            // 使用 Tablesaw 加载数据
            tech.tablesaw.api.Table table = BaseTools.loadCSVTable(csvPath);
            int originalRows = table.rowCount();
            System.out.println("原始行数: " + originalRows);

            // 获取数据质量报告
            String qualityReport = cleaningTools.getDataQualityReport(table);
            System.out.println("数据质量报告: " + qualityReport);

            // 执行数据清洗
            // 1. 处理缺失值
            tech.tablesaw.api.Table cleanedTable = cleaningTools.handleMissingValues(table, "mean", "");
            System.out.println("处理后缺失值");

            // 2. 去除重复行
            int beforeDedup = cleanedTable.rowCount();
            cleanedTable = cleaningTools.removeDuplicates(cleanedTable);
            int duplicatesRemoved = beforeDedup - cleanedTable.rowCount();
            System.out.println("去除重复行: " + duplicatesRemoved);

            // 3. 生成清洗后的质量报告
            String cleanedQualityReport = cleaningTools.getDataQualityReport(cleanedTable);

            String summary = String.format(
                    "数据清洗完成\n\n原始数据: %d 行\n清洗后: %d 行\n去除重复: %d 行\n\n清洗后质量:\n%s",
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
            e.printStackTrace();
            return Map.of(
                    AnalysisState.ERRORS_KEY, List.of("数据清洗失败: " + e.getMessage()),
                    AnalysisState.CURRENT_STEP_KEY, "ERROR"
            );
        }
    }
}
