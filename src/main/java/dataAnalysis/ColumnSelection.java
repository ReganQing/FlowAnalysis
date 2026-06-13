package dataAnalysis;

import dataAnalysis.model.DataProfile;

/**
 * 基于 {@link DataProfile} 的列选择纯函数工具。
 * <p>
 * 用于在缺少显式参数时，从数据画像中按列类型选出合理默认列，
 * 使管线对任意领域的 CSV 都能产出结果，而非依赖硬编码的销售列名。
 */
public final class ColumnSelection {

    private ColumnSelection() {
    }

    /** 第一个日期列；无则返回 null。 */
    public static String firstDate(DataProfile profile) {
        return firstOrNull(profile.dateColumns());
    }

    /** 第一个数值列；无则返回 null。 */
    public static String firstNumeric(DataProfile profile) {
        return firstOrNull(profile.numericColumns());
    }

    /** 第二个数值列；不足则返回 null。 */
    public static String secondNumeric(DataProfile profile) {
        return profile.numericColumns().size() >= 2 ? profile.numericColumns().get(1) : null;
    }

    /** 第一个分类列；无则返回 null。 */
    public static String firstCategorical(DataProfile profile) {
        return firstOrNull(profile.categoricalColumns());
    }

    private static String firstOrNull(java.util.List<String> columns) {
        return columns.isEmpty() ? null : columns.get(0);
    }
}
