package desktop.repository.impl;

import desktop.repository.DatabaseManager;
import desktop.repository.SettingsRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class SQLiteSettingsRepository implements SettingsRepository {

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private final DatabaseManager db;

    public SQLiteSettingsRepository() {
        this.db = DatabaseManager.getInstance();
    }

    @Override
    public Optional<String> get(String key) {
        String sql = "SELECT value FROM app_settings WHERE key = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? Optional.of(rs.getString("value")) : Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("读取设置失败", e);
        }
    }

    @Override
    public void set(String key, String value) {
        String sql = "INSERT OR REPLACE INTO app_settings (key, value, updated_at) VALUES (?,?,?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.setString(3, LocalDateTime.now().format(FMT));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("保存设置失败", e);
        }
    }
}
