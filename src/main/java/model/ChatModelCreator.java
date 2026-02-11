package model;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

public class ChatModelCreator {

    public static OpenAiChatModel newModel() {
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        return OpenAiChatModel.builder()
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .apiKey(apiKey)
                .modelName("qwen-max-latest")
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    public static OpenAiChatModel newModel(String modelName) {
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        return OpenAiChatModel.builder()
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .apiKey(apiKey)
                .modelName("modelName")
                .build();
    }

    public static OpenAiChatModel newModel(String modelName, double temperature) {
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        return OpenAiChatModel.builder()
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .build();
    }

    public static OpenAiChatModel newModel(String modelName, double temperature, int maxTokens) {
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        return OpenAiChatModel.builder()
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();
    }

    public static StreamingChatModel newStreamingModel() {
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        return OpenAiStreamingChatModel.builder()
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .apiKey(apiKey)
                .modelName("qwen-max-latest")
                .build();
    }
}
