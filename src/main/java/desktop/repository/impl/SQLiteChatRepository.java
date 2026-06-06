package desktop.repository.impl;

import desktop.model.ChatMessage;
import desktop.repository.ChatRepository;
import desktop.repository.DatabaseManager;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SQLiteChatRepository implements ChatRepository {

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private final DatabaseManager db;

    public SQLiteChatRepository() {
        this.db = DatabaseManager.getInstance();
    }

    @Override
    public ChatMessage save(ChatMessage message) {
        String sql = "INSERT INTO chat_message (session_id, role, content, model_name, tool_name, tool_input, tool_duration, created_at) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, message.sessionId());
            ps.setString(2, message.role().name());
            ps.setString(3, message.content());
            ps.setString(4, message.modelName());
            ps.setString(5, message.toolName());
            ps.setString(6, message.toolInput());
            if (message.toolDuration() != null) {
                ps.setInt(7, message.toolDuration());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setString(8, message.createdAt().format(FMT));
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return new ChatMessage(
                    keys.getLong(1), message.sessionId(), message.role(),
                    message.content(), message.modelName(), message.toolName(),
                    message.toolInput(), message.toolDuration(), message.createdAt()
                );
            }
            return message;
        } catch (SQLException e) {
            throw new RuntimeException("保存消息失败", e);
        }
    }

    @Override
    public List<ChatMessage> findBySessionId(String sessionId) {
        String sql = "SELECT * FROM chat_message WHERE session_id = ? ORDER BY created_at ASC";
        List<ChatMessage> messages = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sessionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int toolDuration = rs.getInt("tool_duration");
                messages.add(ChatMessage.fromRow(
                    rs.getLong("id"),
                    rs.getString("session_id"),
                    rs.getString("role"),
                    rs.getString("content"),
                    rs.getString("model_name"),
                    rs.getString("tool_name"),
                    rs.getString("tool_input"),
                    rs.wasNull() ? null : toolDuration,
                    rs.getString("created_at")
                ));
            }
            return messages;
        } catch (SQLException e) {
            throw new RuntimeException("查询消息失败", e);
        }
    }

    @Override
    public void saveAll(List<ChatMessage> messages) {
        for (ChatMessage msg : messages) {
            save(msg);
        }
    }
}
