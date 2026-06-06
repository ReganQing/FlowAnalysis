package desktop.repository;

import java.util.Optional;

public interface SettingsRepository {
    Optional<String> get(String key);
    void set(String key, String value);
}
