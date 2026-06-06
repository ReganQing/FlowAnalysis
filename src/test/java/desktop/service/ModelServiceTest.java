package desktop.service;

import desktop.repository.SettingsRepository;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModelServiceTest {

    @Test
    void persistsAndLoadsDefaultModel() {
        InMemorySettingsRepository settings = new InMemorySettingsRepository();
        ModelService service = new ModelService(settings);

        service.setCurrentModel("qwen-plus-latest");

        assertEquals("qwen-plus-latest", service.getCurrentModel().modelName());
    }

    private static class InMemorySettingsRepository implements SettingsRepository {
        private final Map<String, String> values = new HashMap<>();

        @Override
        public Optional<String> get(String key) {
            return Optional.ofNullable(values.get(key));
        }

        @Override
        public void set(String key, String value) {
            values.put(key, value);
        }
    }
}
