package desktop.model;

import java.util.List;

/**
 * 可用模型配置（不可变）。
 */
public record ModelConfig(
    String displayName,
    String modelName,
    String description
) {
    /** 预置模型列表 */
    public static final List<ModelConfig> AVAILABLE_MODELS = List.of(
        new ModelConfig("Qwen Max", "qwen-max-latest", "阿里通义千问旗舰模型，综合能力最强"),
        new ModelConfig("Qwen Plus", "qwen-plus-latest", "能力与速度均衡的通用模型"),
        new ModelConfig("Qwen Turbo", "qwen-turbo-latest", "轻量快速模型"),
        new ModelConfig("Qwen 3.7 Max", "qwen3.7-max", "通义千问 3.7，推理能力突出"),
        new ModelConfig("DeepSeek V4 Pro", "deepseek-v4-pro", "DeepSeek 中等模型，性价比高"),
        new ModelConfig("DeepSeek V4 Flash", "deepseek-v4-flash", "DeepSeek 轻量模型，响应最快")
    );

    public static ModelConfig findByModelName(String modelName) {
        return AVAILABLE_MODELS.stream()
            .filter(m -> m.modelName().equals(modelName))
            .findFirst()
            .orElse(AVAILABLE_MODELS.get(0));
    }
}
