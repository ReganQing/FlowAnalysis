package desktop.service;

/**
 * 流式响应回调接口，由 UI 层实现。
 */
public interface StreamingCallback {

    /** 收到一个新 token */
    void onToken(String token);

    /** AI 回复完成 */
    void onComplete(String fullResponse);

    /** 发生错误 */
    void onError(Throwable error);

    /** 工具调用开始 */
    void onToolCallStart(String toolName, String toolInput);

    /** 工具调用完成 */
    void onToolCallComplete(String toolName, String result, long durationMs);
}
