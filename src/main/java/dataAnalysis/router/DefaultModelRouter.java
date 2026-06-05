package dataAnalysis.router;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;

/**
 * 默认模型路由实现
 * 所有任务使用同一模型，后续阶段实现智能路由
 */
public class DefaultModelRouter implements ModelRouter {

    private final ChatModel chatModel;
    private final StreamingChatModel streamingModel;

    public DefaultModelRouter(ChatModel chatModel) {
        this(chatModel, null);
    }

    public DefaultModelRouter(ChatModel chatModel, StreamingChatModel streamingModel) {
        this.chatModel = chatModel;
        this.streamingModel = streamingModel;
    }

    @Override
    public ChatModel getModelForTask(TaskType taskType) {
        return chatModel;
    }

    @Override
    public StreamingChatModel getStreamingModelForTask(TaskType taskType) {
        return streamingModel;
    }
}
