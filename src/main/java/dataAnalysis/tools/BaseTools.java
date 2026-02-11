package dataAnalysis.tools;

import tech.tablesaw.api.Table;

import java.io.File;

/**
 * 基础工具类 - 提供公共方法
 */
public class BaseTools {

    /**
     * 加载 CSV 文件为 Table
     */
    public static Table loadCSVTable(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                throw new RuntimeException("文件不存在: " + path);
            }
            return Table.read().csv(file);
        } catch (Exception e) {
            throw new RuntimeException("读取 CSV 文件失败: " + e.getMessage(), e);
        }
    }
}
