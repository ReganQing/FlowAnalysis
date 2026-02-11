package model;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaChatRequestParameters;
import dev.langchain4j.service.SystemMessage;

class OllamaAIChatModel {
    OllamaChatModel ollamaChatModel = OllamaChatModel.builder()
            .baseUrl("http://localhost:11434")
            .modelName("gpt-oss:20b")
            .maxRetries(3)
            .responseFormat(ResponseFormat.JSON)
            .logRequests(true)
            .logResponses(true)
            .build();


    @SystemMessage("You are a helpful assistant.")
    public String chat(String message) {
        UserMessage userMessage = UserMessage.from(message);
        ChatRequest chatRequest = ChatRequest.builder()
                .messages(userMessage)
                .parameters(OllamaChatRequestParameters.builder()
                        .temperature(0.7)
                        .topP(0.9)
                        .topK(40)
                        .build())
                .build();
        ChatResponse response = ollamaChatModel.chat(chatRequest);
        return response.aiMessage().text();
    }
}

class OllamaAIChatModelTest {
    public static void main(String[] args) {
        OllamaAIChatModel ollamaAIChatModel = new OllamaAIChatModel();
        String message = "How should we view the moments of separation in life?";
        System.out.println(ollamaAIChatModel.chat(message));
    }
}
