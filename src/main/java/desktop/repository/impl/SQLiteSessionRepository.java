package desktop.repository.impl;

import desktop.model.ChatSession;
import desktop.repository.DatabaseManager;
import desktop.repository.SessionRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteSessionRepository implements SessionRepository {

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private final DatabaseManager db;

    public SQLiteSessionRepository() {
        this.db = DatabaseManager.getInstance();
    }

    @Override
    public ChatSession save(ChatSession session) {
        String sql = "INSERT INTO chat_session (id, title, model_name, created_at, updated_at) VALUES (?,?,?,?,?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, session.id());
            ps.setString(2, session.title());
            ps.setString(3, session.modelName());
            ps.setString(4, session.createdAt().format(FMT));
            ps.setString(5, session.updatedAt().format(FMT));
            ps.executeUpdate();
            return session;
        } catch (SQLException e) {
            throw new RuntimeException("保存会话失败", e);
        }
    }

    @Override
    public Optional<ChatSession> findById(String id) {
        String sql = "SELECT * FROM chat_session WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(ChatSession.fromRow(
                    rs.getString("id"),
                    rs.getString("title"),
                    rs.getString("model_name"),
                    rs.getString("created_at"),
                    rs.getString("updated_at")
                ));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("查询会话失败", e);
        }
    }

    @Override
    public List<ChatSession> findAllOrderByUpdated() {
        String sql = "SELECT * FROM chat_session ORDER BY updated_at DESC";
        List<ChatSession> sessions = new ArrayList<>();
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                sessions.add(ChatSession.fromRow(
                    rs.getString("id"),
                    rs.getString("title"),
                    rs.getString("model_name"),
                    rs.getString("created_at"),
                    rs.getString("updated_at")
                ));
            }
            return sessions;
        } catch (SQLException e) {
            throw new RuntimeException("查询会话列表失败", e);
        }
    }

    @Override
    public ChatSession updateTitle(String sessionId, String newTitle) {
        String sql = "UPDATE chat_session SET title = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String now = LocalDateTime.now().format(FMT);
            ps.setString(1, newTitle);
            ps.setString(2, now);
            ps.setString(3, sessionId);
            ps.executeUpdate();
            return findById(sessionId).orElseThrow();
        } catch (SQLException e) {
            throw new RuntimeException("更新会话标题失败", e);
        }
    }

    @Override
    public void deleteById(String sessionId) {
        String sql = "DELETE FROM chat_session WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sessionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("删除会话失败", e);
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM chat_session";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("统计会话数量失败", e);
        }
    }
}
