package dataAnalysis;

public class NoOpProgressListener implements ProgressListener {
    @Override public void onNodeStart(String nodeName, int stageIndex) {}
    @Override public void onNodeProgress(String nodeName, String message) {}
    @Override public void onNodeComplete(String nodeName, long durationMs) {}
    @Override public void onNodeError(String nodeName, String error) {}
    @Override public void onPipelineComplete(DataAnalysisGraph.AnalysisResult result) {}
    @Override public void onPipelineError(String error) {}
}
