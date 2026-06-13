package dataAnalysis;

import dataAnalysis.model.AnalysisTask;
import dataAnalysis.model.AnalysisType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubTaskProgressTest {

    // plan: [TREND, COMPARISON, CORRELATION, INSIGHT]
    private static final List<AnalysisTask> TASKS = List.of(
        new AnalysisTask("t1", AnalysisType.TREND, "趋势分析", Map.of(), 1),
        new AnalysisTask("t2", AnalysisType.COMPARISON, "对比分析", Map.of(), 2),
        new AnalysisTask("t3", AnalysisType.CORRELATION, "相关性分析", Map.of(), 3),
        new AnalysisTask("t4", AnalysisType.INSIGHT, "AI洞察", Map.of(), 4)
    );

    @Test
    void analysisScopeTotalIsNonInsightCount() {
        assertEquals(3, SubTaskProgress.total(TASKS, SubTaskProgress.Scope.ANALYSIS));
    }

    @Test
    void insightScopeTotalIsInsightCount() {
        assertEquals(1, SubTaskProgress.total(TASKS, SubTaskProgress.Scope.INSIGHT));
    }

    @Test
    void analysisIndexOfSecondTaskIsTwo() {
        // currentTaskIndex = 1 -> COMPARISON -> 第 2 个 ANALYSIS 任务
        assertEquals(2, SubTaskProgress.indexOf(TASKS, 1, SubTaskProgress.Scope.ANALYSIS));
    }

    @Test
    void insightIndexOfFourthTaskIsOne() {
        // currentTaskIndex = 3 -> INSIGHT -> 第 1 个 INSIGHT 任务
        assertEquals(1, SubTaskProgress.indexOf(TASKS, 3, SubTaskProgress.Scope.INSIGHT));
    }

    @Test
    void emptyPlanYieldsZeroTotal() {
        List<AnalysisTask> empty = List.of();
        assertEquals(0, SubTaskProgress.total(empty, SubTaskProgress.Scope.ANALYSIS));
    }
}
