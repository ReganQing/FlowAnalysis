package dataAnalysis.model;

/**
 * 子任务进度事件 —— 用于迭代型节点（analyzer/insight）汇报每个子任务的执行状态。
 *
 * @param index  当前子任务序号（1-based）
 * @param total  该阶段子任务总数
 * @param label  子任务展示名（通常为 {@code AnalysisTask.target()}）
 * @param status 子任务状态
 */
public record SubTaskEvent(int index, int total, String label, TaskStatus status) {

    /** 子任务状态。 */
    public enum TaskStatus { STARTED, COMPLETED, ERROR }
}
