package dataAnalysis.model;

/**
 * 数据处理策略
 * 根据数据量自动选择
 */
public enum ProcessingStrategy {
    FULL_DATA,
    SAMPLE_AND_AGGREGATE,
    AGGREGATE_ONLY
}
