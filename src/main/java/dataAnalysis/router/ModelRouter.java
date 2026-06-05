package dataAnalysis.router;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;

/**
 * 模型路由接口
 * 不同任务类型可分配不同模型，现阶段仅定义接口
 */
public interface ModelRouter {

    ChatModel getModelForTask(TaskType taskType);

    StreamingChatModel getStreamingModelForTask(TaskType taskType);

    enum TaskType {
        PARSING,
        CLEANING,
        PLANNING,
        ANALYSIS,
        VISUALIZATION,
        INSIGHT,
        REPORT
    }
}
