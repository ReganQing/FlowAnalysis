package dataAnalysis;

import dataAnalysis.model.AnalysisTask;
import dataAnalysis.model.AnalysisType;

import java.util.List;

/**
 * 子任务序号/总数计算纯函数。
 * <p>
 * 迭代型节点（analyzer=ANALYSIS scope，insight=INSIGHT scope）按 scope 汇报进度。
 */
public final class SubTaskProgress {

    /** 子任务作用域。 */
    public enum Scope { ANALYSIS, INSIGHT }

    private SubTaskProgress() {
    }

    /** 该 scope 的任务总数。 */
    public static int total(List<AnalysisTask> tasks, Scope scope) {
        return (int) tasks.stream().filter(t -> belongs(t, scope)).count();
    }

    /**
     * 当前任务（currentTaskIndex 处）在其 scope 内的 1-based 序号。
     */
    public static int indexOf(List<AnalysisTask> tasks, int currentTaskIndex, Scope scope) {
        int idx = 0;
        int upper = Math.min(currentTaskIndex + 1, tasks.size());
        for (int i = 0; i < upper; i++) {
            if (belongs(tasks.get(i), scope)) {
                idx++;
            }
        }
        return idx;
    }

    private static boolean belongs(AnalysisTask task, Scope scope) {
        boolean isInsight = task.type() == AnalysisType.INSIGHT;
        return (scope == Scope.INSIGHT) == isInsight;
    }
}
