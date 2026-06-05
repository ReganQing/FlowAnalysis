package dataAnalysis.model;

/**
 * 单列的数据概况
 */
public record ColumnProfile(
    String name,
    String type,
    int missingCount,
    int uniqueCount,
    double missingRate
) {}
