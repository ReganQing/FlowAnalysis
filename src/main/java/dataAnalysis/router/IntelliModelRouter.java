package dataAnalysis.router;

/**
 * @author admin
 * @date 2026/6/6 09:49
 */

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import model.ChatModelCreator;

/**
 * 智能路由，根据任务类型分配模型
 */
public class IntelliModelRouter implements ModelRouter {

    @Override
    public ChatModel getModelForTask(TaskType taskType) {
        return switch (taskType) {
            case PARSING, CLEANING, VISUALIZATION -> ChatModelCreator.newModel("deepseek-v4-flash");
            case PLANNING, INSIGHT -> ChatModelCreator.newModel("qwen3.7-max");
            case ANALYSIS, REPORT -> ChatModelCreator.newModel("deepseek-v4-pro");
        };
    }

    @Override
    public StreamingChatModel getStreamingModelForTask(TaskType taskType) {
        return switch (taskType) {
            case PARSING, CLEANING, VISUALIZATION -> ChatModelCreator.newStreamingModel("deepseek-v4-flash");
            case PLANNING, INSIGHT -> ChatModelCreator.newStreamingModel("qwen3.7-max");
            case ANALYSIS, REPORT -> ChatModelCreator.newStreamingModel("deepseek-v4-pro");
        };
    }

}
