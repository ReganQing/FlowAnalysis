package desktop.service;

import desktop.model.ChatSession;
import desktop.repository.SessionRepository;
import desktop.repository.impl.SQLiteSessionRepository;

import java.util.List;
import java.util.Optional;

/**
 * 会话管理服务。
 */
public class SessionService {

    private final SessionRepository sessionRepo;

    public SessionService() {
        this.sessionRepo = new SQLiteSessionRepository();
    }

    public ChatSession createSession(String modelName) {
        ChatSession session = ChatSession.createNew(modelName);
        return sessionRepo.save(session);
    }

    public Optional<ChatSession> getSession(String sessionId) {
        return sessionRepo.findById(sessionId);
    }

    public List<ChatSession> getAllSessions() {
        return sessionRepo.findAllOrderByUpdated();
    }

    public ChatSession renameSession(String sessionId, String newTitle) {
        return sessionRepo.updateTitle(sessionId, newTitle);
    }

    public void deleteSession(String sessionId) {
        sessionRepo.deleteById(sessionId);
    }

    /**
     * 更新会话的 updated_at 为当前时间（表示最近活跃）。
     */
    public ChatSession touchSession(String sessionId) {
        ChatSession session = sessionRepo.findById(sessionId).orElseThrow();
        return sessionRepo.updateTitle(sessionId, session.title());
    }
}
