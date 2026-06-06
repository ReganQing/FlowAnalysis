package desktop.repository;

import desktop.model.ChatSession;
import java.util.List;
import java.util.Optional;

public interface SessionRepository {
    ChatSession save(ChatSession session);
    Optional<ChatSession> findById(String id);
    List<ChatSession> findAllOrderByUpdated();
    ChatSession updateTitle(String sessionId, String newTitle);
    void deleteById(String sessionId);
    long count();
}
