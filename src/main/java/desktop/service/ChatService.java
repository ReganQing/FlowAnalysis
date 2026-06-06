package desktop.service;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;

import desktop.model.ChatMessage;
import desktop.model.MessageRole;
import desktop.repository.ChatRepository;
import desktop.repository.impl.SQLiteChatRepository;
import model.ChatModelCreator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 聊天核心服务 — 管理对话流、流式输出、工具调用。
 */
public class ChatService {

    private final ChatRepository chatRepo;

    public ChatService() {
        this.chatRepo = new SQLiteChatRepository();
    }

    /**
     * 保存用户消息到数据库。
     */
    public ChatMessage saveUserMessage(String sessionId, String content) {
        ChatMessage msg = new ChatMessage(
            null, sessionId, MessageRole.USER, content,
            null, null, null, null, LocalDateTime.now()
        );
        return chatRepo.save(msg);
    }

    /**
     * 保存 AI 回复到数据库。
     */
    public ChatMessage saveAiMessage(String sessionId, String content, String modelName) {
        ChatMessage msg = new ChatMessage(
            null, sessionId, MessageRole.AI, content,
            modelName, null, null, null, LocalDateTime.now()
        );
        return chatRepo.save(msg);
    }

    /**
     * 保存工具调用消息到数据库。
     */
    public ChatMessage saveToolMessage(String sessionId, String toolName,
                                        String toolInput, String result,
                                        long durationMs, String modelName) {
        ChatMessage msg = new ChatMessage(
            null, sessionId, MessageRole.TOOL, result,
            modelName, toolName, toolInput, (int) durationMs, LocalDateTime.now()
        );
        return chatRepo.save(msg);
    }

    /**
     * 加载会话历史消息。
     */
    public List<ChatMessage> loadHistory(String sessionId) {
        return chatRepo.findBySessionId(sessionId);
    }

    /**
     * 发送消息并获取流式响应。
     */
    public void sendMessage(String sessionId, String userContent,
                            String modelName, StreamingCallback callback) {
        List<ChatMessage> history = loadHistory(sessionId);
        List<dev.langchain4j.data.message.ChatMessage> lc4jMessages = buildLc4jMessages(history, userContent);

        StreamingChatModel streamingModel = ChatModelCreator.newStreamingModel(modelName);

        StringBuilder fullResponse = new StringBuilder();

        streamingModel.chat(lc4jMessages, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String partialResponse) {
                fullResponse.append(partialResponse);
                callback.onToken(partialResponse);
            }

            @Override
            public void onCompleteResponse(ChatResponse response) {
                callback.onComplete(fullResponse.toString());
            }

            @Override
            public void onError(Throwable error) {
                callback.onError(error);
            }
        });
    }

    /**
     * 将数据库消息历史转换为 LangChain4j 消息列表。
     */
    private List<dev.langchain4j.data.message.ChatMessage> buildLc4jMessages(
            List<ChatMessage> history, String currentUserContent) {
        List<dev.langchain4j.data.message.ChatMessage> messages = new ArrayList<>();

        messages.add(SystemMessage.from("你是一个智能助手，可以帮助用户解答问题。请用中文回复。"));

        for (ChatMessage msg : history) {
            switch (msg.role()) {
                case USER -> messages.add(UserMessage.from(msg.content()));
                case AI -> messages.add(AiMessage.from(msg.content()));
                case TOOL -> messages.add(AiMessage.from(
                    "[工具调用: " + msg.toolName() + "] 结果: " + msg.content()));
                default -> {}
            }
        }

        messages.add(UserMessage.from(currentUserContent));

        return messages;
    }
}
