package desktop.repository;

import desktop.model.ChatMessage;
import java.util.List;

public interface ChatRepository {
    ChatMessage save(ChatMessage message);
    List<ChatMessage> findBySessionId(String sessionId);
    void saveAll(List<ChatMessage> messages);
}
