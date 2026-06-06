package desktop.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 聊天消息实体（不可变）。
 */
public record ChatMessage(
    Long id,
    String sessionId,
    MessageRole role,
    String content,
    String modelName,
    String toolName,
    String toolInput,
    Integer toolDuration,
    LocalDateTime createdAt
) {
    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public String formattedTime() {
        return createdAt.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public boolean isToolCall() {
        return role == MessageRole.TOOL;
    }

    /**
     * 从数据库行创建 ChatMessage。
     */
    public static ChatMessage fromRow(
            Long id, String sessionId, String roleStr, String content,
            String modelName, String toolName, String toolInput,
            Integer toolDuration, String createdAtStr) {
        return new ChatMessage(
            id, sessionId,
            MessageRole.valueOf(roleStr),
            content, modelName, toolName, toolInput, toolDuration,
            LocalDateTime.parse(createdAtStr, FORMATTER)
        );
    }
}
