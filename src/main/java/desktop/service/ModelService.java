package desktop.service;

import desktop.model.ModelConfig;
import desktop.repository.SettingsRepository;
import desktop.repository.impl.SQLiteSettingsRepository;

import java.util.List;

/**
 * 模型管理服务。
 */
public class ModelService {

    private static final String SETTINGS_KEY_DEFAULT_MODEL = "default_model";
    private final SettingsRepository settingsRepo;

    public ModelService() {
        this.settingsRepo = new SQLiteSettingsRepository();
    }

    public List<ModelConfig> getAvailableModels() {
        return ModelConfig.AVAILABLE_MODELS;
    }

    public ModelConfig getCurrentModel() {
        String modelName = settingsRepo.get(SETTINGS_KEY_DEFAULT_MODEL)
            .orElse("qwen-max-latest");
        return ModelConfig.findByModelName(modelName);
    }

    public void setCurrentModel(String modelName) {
        settingsRepo.set(SETTINGS_KEY_DEFAULT_MODEL, modelName);
    }
}
