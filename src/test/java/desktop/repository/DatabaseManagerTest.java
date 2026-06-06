package desktop.repository;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseManagerTest {

    @Test
    void enablesForeignKeysForEveryConnection() throws Exception {
        try (Connection connection = DatabaseManager.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA foreign_keys")) {
            assertEquals(1, resultSet.getInt(1));
        }
    }
}
