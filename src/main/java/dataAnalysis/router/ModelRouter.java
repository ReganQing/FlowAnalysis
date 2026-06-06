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
        PARSING,        // 数据解析 - 可用轻量模型 deepseek-v4-flash
        CLEANING,       // 数据清洗 - 可用轻量模型 deepseek-v4-flash
        PLANNING,       // 分析规划 - 需要强推理模型 qwen3.7-max
        ANALYSIS,       // 数据分析 - 中等模型 deepseek-v4-pro
        VISUALIZATION,  // 图表生成 - 轻量模型 deepseek-v4-flash
        INSIGHT,        // 智能洞察 - 需要强推理模型 qwen3.7-max
        REPORT          // 报告生成 - 中等模型 deepseek-v4-pro
    }
}
