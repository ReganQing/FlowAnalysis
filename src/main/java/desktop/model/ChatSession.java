package desktop.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 聊天会话实体（不可变）。
 */
public record ChatSession(
    String id,
    String title,
    String modelName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public static ChatSession createNew(String modelName) {
        LocalDateTime now = LocalDateTime.now();
        return new ChatSession(
            UUID.randomUUID().toString(),
            "新对话",
            modelName,
            now, now
        );
    }

    public ChatSession withTitle(String newTitle) {
        return new ChatSession(id, newTitle, modelName, createdAt, updatedAt);
    }

    public ChatSession withUpdatedTime() {
        return new ChatSession(id, title, modelName, createdAt, LocalDateTime.now());
    }

    public String formattedTime() {
        return updatedAt.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public static ChatSession fromRow(
            String id, String title, String modelName,
            String createdAtStr, String updatedAtStr) {
        return new ChatSession(
            id, title, modelName,
            LocalDateTime.parse(createdAtStr, FORMATTER),
            LocalDateTime.parse(updatedAtStr, FORMATTER)
        );
    }
}
