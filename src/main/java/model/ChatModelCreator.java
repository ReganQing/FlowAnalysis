package model;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

public class ChatModelCreator {

    private static final String BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    private static String requireApiKey() {
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                "DASHSCOPE_API_KEY 环境变量未设置，请设置后重试");
        }
        return apiKey;
    }

    public static OpenAiChatModel newModel() {
        return OpenAiChatModel.builder()
                .baseUrl(BASE_URL)
                .apiKey(requireApiKey())
                .modelName("qwen-max-latest")
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    public static OpenAiChatModel newModel(String modelName) {
        return OpenAiChatModel.builder()
                .baseUrl(BASE_URL)
                .apiKey(requireApiKey())
                .modelName(modelName)
                .build();
    }

    public static OpenAiChatModel newModel(String modelName, double temperature) {
        return OpenAiChatModel.builder()
                .baseUrl(BASE_URL)
                .apiKey(requireApiKey())
                .modelName(modelName)
                .temperature(temperature)
                .build();
    }

    public static OpenAiChatModel newModel(String modelName, double temperature, int maxTokens) {
        return OpenAiChatModel.builder()
                .baseUrl(BASE_URL)
                .apiKey(requireApiKey())
                .modelName(modelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();
    }

    public static StreamingChatModel newStreamingModel() {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(BASE_URL)
                .apiKey(requireApiKey())
                .modelName("qwen-max-latest")
                .build();
    }

    public static StreamingChatModel newStreamingModel(String modelName) {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(BASE_URL)
                .apiKey(requireApiKey())
                .modelName(modelName)
                .build();
    }
}
