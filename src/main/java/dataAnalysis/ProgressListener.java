package dataAnalysis;

/**
 * 数据分析管线进度监听器。
 * <p>
 * 所有回调均在调用者线程上执行；
 * 若需在 JavaFX Application Thread 更新 UI，请使用 {@code Platform.runLater()}。
 */
public interface ProgressListener {

    /**
     * 节点开始执行。
     *
     * @param nodeName   节点名称（parser / cleaner / planner / analyzer / insight / chart / report）
     * @param stageIndex 阶段序号（1-7）
     */
    void onNodeStart(String nodeName, int stageIndex);

    /**
     * 节点执行过程中的进度消息。
     *
     * @param nodeName 节点名称
     * @param message  进度描述
     */
    void onNodeProgress(String nodeName, String message);

    /**
     * 节点执行完成。
     *
     * @param nodeName   节点名称
     * @param durationMs 执行耗时（毫秒）
     */
    void onNodeComplete(String nodeName, long durationMs);

    /**
     * 节点执行出错（非致命，管线可能继续）。
     *
     * @param nodeName 节点名称
     * @param error    错误描述
     */
    void onNodeError(String nodeName, String error);

    /**
     * 整条管线执行完成。
     *
     * @param result 分析结果
     */
    void onPipelineComplete(DataAnalysisGraph.AnalysisResult result);

    /**
     * 管线执行失败（致命错误）。
     *
     * @param error 错误描述
     */
    void onPipelineError(String error);
}
