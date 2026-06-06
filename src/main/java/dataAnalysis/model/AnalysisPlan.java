package dataAnalysis.model;

import java.util.List;

/**
 * AI生成的分析计划
 * 包含要执行的分析任务列表和规划理由
 */
public record AnalysisPlan(
    List<AnalysisTask> tasks,
    String reasoning,
    List<String> suggestedCharts,
    int currentTaskIndex
) implements java.io.Serializable {
    public String nextStep() {
        if (currentTaskIndex >= tasks.size()) {
            return "chart";
        }
        AnalysisTask task = tasks.get(currentTaskIndex);
        return switch (task.type()) {
            case TREND, DISTRIBUTION, CORRELATION, COMPARISON, OUTLIER -> "analyzer";
            case INSIGHT -> "insight";
        };
    }

    public boolean hasMoreTasks() {
        return currentTaskIndex < tasks.size();
    }

    public AnalysisPlan advance() {
        return new AnalysisPlan(tasks, reasoning, suggestedCharts, currentTaskIndex + 1);
    }

    public AnalysisTask currentTask() {
        if (currentTaskIndex >= tasks.size()) return null;
        return tasks.get(currentTaskIndex);
    }
}
