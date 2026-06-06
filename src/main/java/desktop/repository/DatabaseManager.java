package desktop.repository;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * SQLite 数据库管理器，负责初始化和连接。
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:data/assistant.db";
    private static final String SCHEMA_PATH = "/desktop/db/schema.sql";

    private static DatabaseManager instance;

    private DatabaseManager() {
        ensureDataDirectory();
        initDatabase();
    }

    private void ensureDataDirectory() {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            Connection connection = DriverManager.getConnection(DB_URL);
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
            } catch (SQLException e) {
                connection.close();
                throw e;
            }
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败", e);
        }
    }

    private void initDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            String schema = loadSchema();
            for (String sql : schema.split(";")) {
                String trimmed = sql.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("数据库初始化失败", e);
        }
    }

    private String loadSchema() {
        try (InputStream is = getClass().getResourceAsStream(SCHEMA_PATH);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException("加载 schema.sql 失败", e);
        }
    }
}
