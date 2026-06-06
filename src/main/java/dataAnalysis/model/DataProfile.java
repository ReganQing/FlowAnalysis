package dataAnalysis.model;

import java.util.List;

/**
 * 整体数据概况
 * 轻量级扫描结果，不加载全量数据
 */
public record DataProfile(
    int rowCount,
    int columnCount,
    List<ColumnProfile> columns,
    List<String> numericColumns,
    List<String> categoricalColumns,
    List<String> dateColumns
) implements java.io.Serializable {
    public String toSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("数据概况: %d 行 × %d 列\n", rowCount, columnCount));
        sb.append("列信息:\n");
        for (ColumnProfile col : columns) {
            sb.append(String.format("  - %s (%s): 唯一值%d, 缺失率%.1f%%\n",
                col.name(), col.type(), col.uniqueCount(), col.missingRate() * 100));
        }
        if (!numericColumns.isEmpty()) {
            sb.append("数值列: ").append(String.join(", ", numericColumns)).append("\n");
        }
        if (!categoricalColumns.isEmpty()) {
            sb.append("分类列: ").append(String.join(", ", categoricalColumns)).append("\n");
        }
        if (!dateColumns.isEmpty()) {
            sb.append("日期列: ").append(String.join(", ", dateColumns)).append("\n");
        }
        return sb.toString();
    }
}
