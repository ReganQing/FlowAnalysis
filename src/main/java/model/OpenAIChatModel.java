package model;

import dev.langchain4j.model.openai.OpenAiChatModel;

public class OpenAIChatModel {
    String apiKey = System.getenv("DASHSCOPE_API_KEY");
    OpenAiChatModel model = OpenAiChatModel.builder()
            .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
            .apiKey(apiKey)
            .modelName("qwen-max-latest")
            .build();

    public String chat(String message) {
        return model.chat(message);
    }
}
