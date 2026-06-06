package desktop.service;

import dataAnalysis.DataAnalysisGraph;
import dataAnalysis.ProgressListener;
import javafx.application.Platform;

import java.util.concurrent.*;

/**
 * 数据分析服务 — 封装 {@link DataAnalysisGraph}，管理后台线程，
 * 确保所有 {@link ProgressListener} 回调在 JavaFX Application Thread 上执行。
 */
public class AnalysisService {

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "data-analysis-worker");
        t.setDaemon(true);
        return t;
    });

    private volatile Future<?> currentTask;
    private volatile boolean cancelled = false;

    /**
     * 异步执行数据分析管线。
     * <p>
     * 所有 listener 回调自动包装在 {@code Platform.runLater()} 中，
     * 确保在 JavaFX Application Thread 上执行。
     *
     * @param filePath 数据文件路径（.csv / .xls / .xlsx）
     * @param listener 进度监听器（回调在 FX 线程）
     */
    public void analyzeFile(String filePath, ProgressListener listener) {
        cancelled = false;

        ProgressListener fxListener = wrapForFxThread(listener);

        currentTask = executor.submit(() -> {
            try {
                DataAnalysisGraph graph = new DataAnalysisGraph();
                graph.execute(filePath, fxListener);
            } catch (Exception e) {
                Platform.runLater(() -> fxListener.onPipelineError(
                    "分析执行失败: " + e.getMessage()));
            }
        });
    }

    /**
     * 取消正在执行的分析任务。
     */
    public void cancelAnalysis() {
        cancelled = true;
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
        }
    }

    /**
     * 是否有分析任务正在运行。
     */
    public boolean isRunning() {
        return currentTask != null && !currentTask.isDone();
    }

    /**
     * 将 ProgressListener 的所有方法调用包装到 Platform.runLater() 中。
     */
    private static ProgressListener wrapForFxThread(ProgressListener delegate) {
        return new ProgressListener() {
            @Override
            public void onNodeStart(String nodeName, int stageIndex) {
                Platform.runLater(() -> delegate.onNodeStart(nodeName, stageIndex));
            }

            @Override
            public void onNodeProgress(String nodeName, String message) {
                Platform.runLater(() -> delegate.onNodeProgress(nodeName, message));
            }

            @Override
            public void onNodeComplete(String nodeName, long durationMs) {
                Platform.runLater(() -> delegate.onNodeComplete(nodeName, durationMs));
            }

            @Override
            public void onNodeError(String nodeName, String error) {
                Platform.runLater(() -> delegate.onNodeError(nodeName, error));
            }

            @Override
            public void onPipelineComplete(DataAnalysisGraph.AnalysisResult result) {
                Platform.runLater(() -> delegate.onPipelineComplete(result));
            }

            @Override
            public void onPipelineError(String error) {
                Platform.runLater(() -> delegate.onPipelineError(error));
            }
        };
    }
}
