package dataAnalysis.nodes;

import dataAnalysis.model.AnalysisTask;
import dataAnalysis.model.AnalysisType;
import dataAnalysis.model.DataProfile;
import org.junit.jupiter.api.Test;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DataAnalyzerNodeTest {

    private DataAnalyzerNode node() {
        return new DataAnalyzerNode();   // package-private test constructor
    }

    private AnalysisTask task(AnalysisType type, Map<String, String> params) {
        return new AnalysisTask("t_" + type.name(), type, type.name() + " 测试", params, 1);
    }

    // ── 错误分支：缺少所需列时返回 {"error": ...} ──

    @Test
    void trendReturnsErrorWhenDateColumnMissing() {
        // profile 有数值列但无日期列
        DataProfile profile = new DataProfile(3, 1, List.of(),
            List.of("amount"), List.of(), List.of());   // 无 dateColumns
        AnalysisTask task = task(AnalysisType.TREND, Map.of("valueColumn", "amount"));

        String result = node().executeTask(Table.create(), task, profile);

        assertTrue(result.contains("\"error\""),
            "缺少日期列时应返回 error JSON，实际: " + result);
        assertTrue(result.contains("日期") || result.contains("数值"),
            "错误信息应说明缺少的列类型，实际: " + result);
    }

    @Test
    void correlationReturnsErrorWhenOnlyOneNumericColumn() {
        // profile 只有 1 个数值列，相关性/离群分析需要 2 个
        DataProfile profile = new DataProfile(3, 1, List.of(),
            List.of("amount"), List.of(), List.of());
        AnalysisTask task = task(AnalysisType.CORRELATION, Map.of());

        String result = node().executeTask(Table.create(), task, profile);

        assertTrue(result.contains("\"error\""),
            "数值列不足时应返回 error JSON，实际: " + result);
        assertTrue(result.contains("数值列不足"),
            "应提示数值列不足，实际: " + result);
    }

    @Test
    void comparisonReturnsErrorWhenCategoricalColumnMissing() {
        // profile 有数值列但无分类列
        DataProfile profile = new DataProfile(3, 1, List.of(),
            List.of("amount"), List.of(), List.of());
        AnalysisTask task = task(AnalysisType.COMPARISON, Map.of("valueColumn", "amount"));

        String result = node().executeTask(Table.create(), task, profile);

        assertTrue(result.contains("\"error\""),
            "缺少分类列时应返回 error JSON，实际: " + result);
    }

    // ── 成功分支：列齐全时调用工具，不返回 error ──

    @Test
    void comparisonSucceedsWithGroupAndValueColumns() {
        Table table = Table.create(
            StringColumn.create("region", new String[]{"北京", "上海", "北京"}),
            IntColumn.create("amount", new int[]{100, 200, 150})
        );
        DataProfile profile = new DataProfile(3, 2, List.of(),
            List.of("amount"), List.of("region"), List.of());
        AnalysisTask task = task(AnalysisType.COMPARISON, Map.of());

        String result = node().executeTask(table, task, profile);

        assertTrue(!result.contains("\"error\""),
            "列齐全时不应返回 error，实际: " + result);
    }
}
