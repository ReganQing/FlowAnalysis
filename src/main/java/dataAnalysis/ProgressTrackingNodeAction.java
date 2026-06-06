package dataAnalysis;

import org.bsc.langgraph4j.action.NodeAction;

final class ProgressTrackingNodeAction {

    private ProgressTrackingNodeAction() {
    }

    static NodeAction<AnalysisState> wrap(String nodeName, int stageIndex,
                                          NodeAction<AnalysisState> delegate,
                                          ProgressListener listener) {
        if (listener == null) {
            return delegate;
        }
        return state -> {
            long startedAt = System.nanoTime();
            listener.onNodeStart(nodeName, stageIndex);
            listener.onNodeProgress(nodeName, "正在执行 " + nodeName);
            try {
                var result = delegate.apply(state);
                long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
                listener.onNodeComplete(nodeName, durationMs);
                return result;
            } catch (Exception e) {
                listener.onNodeError(nodeName, e.getMessage());
                throw e;
            }
        };
    }
}
