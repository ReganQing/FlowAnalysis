package dataAnalysis.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import tech.tablesaw.api.Table;
import tech.tablesaw.api.NumberColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.columns.Column;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;

/**
 * 数据清洗工具
 * 提供缺失值处理、异常值检测、去重等功能
 */
public class DataCleaningTools {

    /**
     * 处理缺失值
     */
    @Tool("处理数据表中的缺失值，支持多种策略: remove(删除行), mean(均值填充), median(中位数填充), mode(众数填充), forward(前向填充)")
    public Table handleMissingValues(
            @P("数据表") Table data,
            @P("策略类型: remove/mean/median/mode/forward") String strategy,
            @P("列名(可选，不指定则处理所有列)") String columnName) {

        Table result = data.copy();

        if ("remove".equalsIgnoreCase(strategy)) {
            // 删除包含缺失值的行
            result = result.dropRowsWithMissingValues();
        } else if ("forward".equalsIgnoreCase(strategy)) {
            // 前向填充
            for (Column<?> col : result.columns()) {
                if (columnName == null || columnName.isEmpty() || col.name().equals(columnName)) {
                    if (col instanceof NumberColumn) {
                        NumberColumn numCol = (NumberColumn) col;
                        for (int i = 1; i < numCol.size(); i++) {
                            if (numCol.isMissing(i)) {
                                numCol.set(i, numCol.get(i - 1));
                            }
                        }
                    } else if (col instanceof StringColumn) {
                        StringColumn strCol = (StringColumn) col;
                        for (int i = 1; i < strCol.size(); i++) {
                            if (strCol.isMissing(i)) {
                                strCol.set(i, strCol.get(i - 1));
                            }
                        }
                    }
                }
            }
        } else {
            // 数值列使用统计值填充
            for (Column<?> col : result.columns()) {
                if (columnName == null || columnName.isEmpty() || col.name().equals(columnName)) {
                    if (col instanceof NumberColumn) {
                        NumberColumn numCol = (NumberColumn) col;
                        if (!numCol.isEmpty()) {
                            if ("mean".equalsIgnoreCase(strategy)) {
                                numCol.setMissingTo(numCol.mean());
                            } else if ("median".equalsIgnoreCase(strategy)) {
                                numCol.setMissingTo(numCol.median());
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * 检测异常值 (使用 IQR 方法)
     */
    @Tool("检测数值列中的异常值，使用 IQR (四分位距) 方法")
    public String detectOutliers(
            @P("数据表") Table data,
            @P("数值列名") String columnName) {

        Column<?> col = data.column(columnName);
        if (!(col instanceof NumberColumn)) {
            return String.format("{\"error\": \"列 %s 不是数值类型\"}", columnName);
        }
        NumberColumn numCol = (NumberColumn) col;

        List<Integer> outlierIndices = new ArrayList<>();
        List<Double> outlierValues = new ArrayList<>();

        // 计算 IQR
        double q1 = numCol.percentile(25);
        double q3 = numCol.percentile(75);
        double iqr = q3 - q1;
        double lowerBound = q1 - 1.5 * iqr;
        double upperBound = q3 + 1.5 * iqr;

        // 检测异常值
        for (int i = 0; i < numCol.size(); i++) {
            if (!numCol.isMissing(i)) {
                double val = numCol.getDouble(i);
                if (val < lowerBound || val > upperBound) {
                    outlierIndices.add(i);
                    outlierValues.add(val);
                }
            }
        }

        return String.format(
                "{\"column\": \"%s\", \"outlier_count\": %d, \"outlier_values\": %s, \"lower_bound\": %.2f, \"upper_bound\": %.2f}",
                columnName, outlierIndices.size(), outlierValues, lowerBound, upperBound);
    }

    /**
     * 移除异常值
     */
    @Tool("移除指定列中的异常值（使用 IQR 方法）")
    public Table removeOutliers(
            @P("数据表") Table data,
            @P("数值列名") String columnName) {

        Table result = data.copy();
        Column<?> col = result.column(columnName);

        if (!(col instanceof NumberColumn)) {
            throw new IllegalArgumentException("列 " + columnName + " 不是数值类型");
        }
        NumberColumn numCol = (NumberColumn) col;

        double q1 = numCol.percentile(25);
        double q3 = numCol.percentile(75);
        double iqr = q3 - q1;
        double lowerBound = q1 - 1.5 * iqr;
        double upperBound = q3 + 1.5 * iqr;

        // 收集要保留的行索引
        List<Integer> rowsToKeep = new ArrayList<>();
        for (int i = 0; i < numCol.size(); i++) {
            if (numCol.isMissing(i) ||
                    (numCol.getDouble(i) >= lowerBound && numCol.getDouble(i) <= upperBound)) {
                rowsToKeep.add(i);
            }
        }

        // Convert List<Integer> to int[]
        int[] rowsArray = rowsToKeep.stream().mapToInt(Integer::intValue).toArray();
        return result.rows(rowsArray);
    }

    /**
     * 去除重复行
     */
    @Tool("去除数据表中的重复行")
    public Table removeDuplicates(@P("数据表") Table data) {
        return data.dropDuplicateRows();
    }

    /**
     * 数据类型转换
     */
    @Tool("将指定列转换为指定类型: string, integer, long, float, double, boolean, date")
    public Table convertColumnTypes(
            @P("数据表") Table data,
            @P("列名和类型映射，格式: 列名:类型,...") String typeMapping) {

        Table result = data.copy();
        String[] mappings = typeMapping.split(",");

        for (String mapping : mappings) {
            String[] parts = mapping.trim().split(":");
            if (parts.length != 2) continue;

            String colName = parts[0].trim();
            String targetType = parts[1].trim().toLowerCase();

            try {
                switch (targetType) {
                    case "integer" -> {
                        if (result.column(colName) instanceof StringColumn) {
                            result.intColumn(colName);
                        }
                    }
                    case "long" -> {
                        if (result.column(colName) instanceof StringColumn) {
                            result.longColumn(colName);
                        }
                    }
                    case "float" -> {
                        if (result.column(colName) instanceof StringColumn) {
                            result.floatColumn(colName);
                        }
                    }
                    case "double" -> {
                        if (result.column(colName) instanceof StringColumn) {
                            result.doubleColumn(colName);
                        }
                    }
                    case "boolean" -> {
                        if (result.column(colName) instanceof StringColumn) {
                            result.booleanColumn(colName);
                        }
                    }
                    case "date" -> {
                        if (result.column(colName) instanceof StringColumn) {
                            result.dateColumn(colName);
                        }
                    }
                    case "string" -> {
                        result.stringColumn(colName);
                    }
                }
            } catch (Exception e) {
                System.err.println("转换列 " + colName + " 失败: " + e.getMessage());
            }
        }

        return result;
    }

    /**
     * 获取数据质量报告
     */
    @Tool("生成数据质量报告，包括缺失值、重复行、数据类型等信息")
    public String getDataQualityReport(@P("数据表") Table data) {
        StringBuilder report = new StringBuilder();
        report.append("{");

        // 基本信息
        report.append("\"total_rows\": ").append(data.rowCount()).append(", ");
        report.append("\"total_columns\": ").append(data.columnCount()).append(", ");

        // 缺失值统计
        int totalMissing = 0;
        for (Column<?> col : data.columns()) {
            totalMissing += col.countMissing();
        }
        report.append("\"total_missing_values\": ").append(totalMissing).append(", ");

        // 重复行
        int uniqueRows = data.dropDuplicateRows().rowCount();
        int duplicateRows = data.rowCount() - uniqueRows;
        report.append("\"duplicate_rows\": ").append(duplicateRows).append(", ");

        // 每列详情
        report.append("\"columns\": [");
        for (int i = 0; i < data.columnCount(); i++) {
            if (i > 0) report.append(", ");
            Column<?> col = data.column(i);
            report.append("{");
            report.append("\"name\": \"").append(col.name()).append("\", ");
            report.append("\"type\": \"").append(col.type().name()).append("\", ");
            report.append("\"missing\": ").append(col.countMissing());
            report.append("}");
        }
        report.append("]}");

        return report.toString();
    }
}
