package dataAnalysis.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * CSV 文件处理工具
 * 提供 CSV 文件读取、解析、编码检测等功能
 */
public class CSVTools {

    /**
     * 读取 CSV 文件内容为 Tablesaw Table
     */
    @Tool("读取 CSV 文件并返回数据表结构")
    public String parseCSV(@P("CSV 文件路径") String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                return String.format("{\"error\": \"文件不存在: %s\"}", path);
            }

            // 使用 Tablesaw 读取 CSV (使用默认编码)
            Table table = Table.read().csv(file);

            // 返回数据摘要
            StringBuilder summary = new StringBuilder();
            summary.append(String.format("{\"success\": true, \"rows\": %d, \"columns\": %d, ",
                    table.rowCount(), table.columnCount()));

            // 添加列信息
            summary.append("\"columns\": [");
            for (int i = 0; i < table.columnCount(); i++) {
                if (i > 0) summary.append(", ");
                summary.append(String.format("{\"name\": \"%s\", \"type\": \"%s\"}",
                        table.column(i).name(), table.column(i).type().name()));
            }
            summary.append("]}");

            return summary.toString();
        } catch (Exception e) {
            return String.format("{\"error\": \"读取文件失败: %s\"}", e.getMessage());
        }
    }

    /**
     * 读取 CSV 文件并返回完整数据
     */
    @Tool("读取 CSV 文件并返回完整数据内容")
    public Table loadCSVTable(@P("CSV 文件路径") String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                throw new RuntimeException("文件不存在: " + path);
            }

            // 使用 Tablesaw 读取 CSV (使用默认编码)
            return Table.read().csv(file);
        } catch (Exception e) {
            throw new RuntimeException("读取 CSV 文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检测 CSV 文件编码格式
     */
    @Tool("检测文件的编码格式(UTF-8, GBK等)")
    public String detectEncoding(@P("文件路径") String path) {
        try {
            // 尝试 UTF-8
            byte[] content = Files.readAllBytes(Path.of(path));

            // 简单的 BOM 检测
            if (content.length >= 3 && content[0] == (byte) 0xEF &&
                    content[1] == (byte) 0xBB && content[2] == (byte) 0xBF) {
                return "UTF-8";
            }

            // 尝试 UTF-8 解码
            try {
                String utf8 = new String(content, StandardCharsets.UTF_8);
                // 检查是否有替换字符（表示解码失败）
                if (!utf8.contains("\uFFFD")) {
                    // 进一步验证：检查是否全是可打印字符
                    return "UTF-8";
                }
            } catch (Exception ignored) {
            }

            // 尝试 GBK
            try {
                String gbk = new String(content, Charset.forName("GBK"));
                if (!gbk.contains("\uFFFD")) {
                    return "GBK";
                }
            } catch (Exception ignored) {
            }

            // 默认 UTF-8
            return "UTF-8";
        } catch (IOException e) {
            return "UTF-8"; // 默认
        }
    }

    /**
     * 获取 CSV 文件的列名
     */
    @Tool("获取 CSV 文件的所有列名")
    public String getColumnNames(@P("CSV 文件路径") String path) {
        try {
            Table table = loadCSVTable(path);
            StringBuilder result = new StringBuilder();
            result.append("[");
            for (int i = 0; i < table.columnCount(); i++) {
                if (i > 0) result.append(", ");
                result.append("\"").append(table.column(i).name()).append("\"");
            }
            result.append("]");
            return result.toString();
        } catch (Exception e) {
            return String.format("{\"error\": \"%s\"}", e.getMessage());
        }
    }

    /**
     * 获取 CSV 文件的统计信息
     */
    @Tool("获取 CSV 文件的基本统计信息，包括行数、列数、缺失值等")
    public String getCSVStatistics(@P("CSV 文件路径") String path) {
        try {
            Table table = loadCSVTable(path);
            StringBuilder stats = new StringBuilder();
            stats.append("{");
            stats.append("\"total_rows\": ").append(table.rowCount()).append(", ");
            stats.append("\"total_columns\": ").append(table.columnCount()).append(", ");

            // 缺失值统计
            stats.append("\"missing_values\": {");
            for (int i = 0; i < table.columnCount(); i++) {
                if (i > 0) stats.append(", ");
                String colName = table.column(i).name();
                int missing = table.column(i).countMissing();
                stats.append("\"").append(colName).append("\": ").append(missing);
            }
            stats.append("}}");

            return stats.toString();
        } catch (Exception e) {
            return String.format("{\"error\": \"%s\"}", e.getMessage());
        }
    }
}
