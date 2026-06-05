package dataAnalysis.model;

/**
 * 采样策略
 */
public enum SampleStrategy {
    RANDOM,
    STRATIFIED,
    TEMPORAL_WINDOW,
    HEAD_TAIL,
    ANOMALY_FOCUSED
}
