package dataAnalysis.model;

import java.util.Map;

/**
 * 单个分析任务
 * 由AI规划器生成，描述一个具体的分析操作
 */
public record AnalysisTask(
    String taskId,
    AnalysisType type,
    String target,
    Map<String, String> parameters,
    int priority
) {}
