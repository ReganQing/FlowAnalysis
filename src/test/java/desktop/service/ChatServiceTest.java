package desktop.service;

import desktop.model.ChatMessage;
import desktop.model.MessageRole;
import desktop.repository.ChatRepository;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ChatServiceTest {

    @Test
    void doesNotAppendCurrentUserMessageWhenItIsAlreadyInHistory() throws Exception {
        ChatMessage currentMessage = new ChatMessage(
            1L, "session-1", MessageRole.USER, "解释这段代码",
            null, null, null, null, LocalDateTime.now()
        );

        List<dev.langchain4j.data.message.ChatMessage> messages =
            invokeBuildMessages(List.of(currentMessage), currentMessage.content());

        long userMessageCount = messages.stream()
            .filter(UserMessage.class::isInstance)
            .count();
        assertEquals(1, userMessageCount);
    }

    @Test
    void reportsSynchronousModelStartupFailureThroughCallback() {
        RuntimeException startupFailure = new RuntimeException("missing API key");
        ChatService service = new ChatService(emptyRepository(), modelName -> {
            throw startupFailure;
        });
        AtomicReference<Throwable> reportedError = new AtomicReference<>();

        assertDoesNotThrow(() -> service.sendMessage(
            "session-1", "hello", "qwen-max-latest",
            callbackReportingTo(reportedError)));

        assertSame(startupFailure, reportedError.get());
    }

    @Test
    void forwardsPartialResponsesBeforeCompletion() {
        ChatService service = new ChatService(emptyRepository(), modelName -> streamingModel("你", "好"));
        List<String> events = new ArrayList<>();

        service.sendMessage("session-1", "hello", "qwen-max-latest", new StreamingCallback() {
            @Override public void onToken(String token) { events.add("token:" + token); }
            @Override public void onComplete(String fullResponse) { events.add("complete:" + fullResponse); }
            @Override public void onError(Throwable error) { events.add("error"); }
            @Override public void onToolCallStart(String toolName, String toolInput) {}
            @Override public void onToolCallComplete(String toolName, String result, long durationMs) {}
        });

        assertEquals(List.of("token:你", "token:好", "complete:你好"), events);
    }

    @Test
    void usesFinalResponseWhenProviderDoesNotSendPartialTokens() {
        ChatService service = new ChatService(emptyRepository(), modelName -> finalOnlyStreamingModel("最终回复"));
        AtomicReference<String> completed = new AtomicReference<>();

        service.sendMessage("session-1", "hello", "qwen-max-latest", new StreamingCallback() {
            @Override public void onToken(String token) {}
            @Override public void onComplete(String fullResponse) { completed.set(fullResponse); }
            @Override public void onError(Throwable error) {}
            @Override public void onToolCallStart(String toolName, String toolInput) {}
            @Override public void onToolCallComplete(String toolName, String result, long durationMs) {}
        });

        assertEquals("最终回复", completed.get());
    }

    @SuppressWarnings("unchecked")
    private List<dev.langchain4j.data.message.ChatMessage> invokeBuildMessages(
            List<ChatMessage> history, String currentUserContent) throws Exception {
        Method method = ChatService.class.getDeclaredMethod(
            "buildLc4jMessages", List.class, String.class);
        method.setAccessible(true);
        return (List<dev.langchain4j.data.message.ChatMessage>) method.invoke(
            new ChatService(), history, currentUserContent);
    }

    private ChatRepository emptyRepository() {
        return new ChatRepository() {
            @Override
            public ChatMessage save(ChatMessage message) {
                return message;
            }

            @Override
            public List<ChatMessage> findBySessionId(String sessionId) {
                return List.of();
            }

            @Override
            public void saveAll(List<ChatMessage> messages) {
            }
        };
    }

    private StreamingCallback callbackReportingTo(AtomicReference<Throwable> reportedError) {
        return new StreamingCallback() {
            @Override public void onToken(String token) {}
            @Override public void onComplete(String fullResponse) {}
            @Override public void onError(Throwable error) { reportedError.set(error); }
            @Override public void onToolCallStart(String toolName, String toolInput) {}
            @Override public void onToolCallComplete(String toolName, String result, long durationMs) {}
        };
    }

    private StreamingChatModel streamingModel(String... tokens) {
        return new StreamingChatModel() {
            @Override
            public void doChat(ChatRequest request, StreamingChatResponseHandler handler) {
                for (String token : tokens) {
                    handler.onPartialResponse(token);
                }
                handler.onCompleteResponse(ChatResponse.builder()
                    .aiMessage(AiMessage.from(String.join("", tokens)))
                    .build());
            }
        };
    }

    private StreamingChatModel finalOnlyStreamingModel(String response) {
        return new StreamingChatModel() {
            @Override
            public void doChat(ChatRequest request, StreamingChatResponseHandler handler) {
                handler.onCompleteResponse(ChatResponse.builder()
                    .aiMessage(AiMessage.from(response))
                    .build());
            }
        };
    }
}
