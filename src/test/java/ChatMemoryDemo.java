import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import model.ChatModelCreator;

public class ChatMemoryDemo {

    interface Assistant {

        String chat(String message);
    }

    public static void main(String[] args) {
        String apiKey = System.getenv("DASHSCOPE_API_KEY");

        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        ChatModel model = ChatModelCreator.newModel();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(chatMemory)
                .build();

        String answer = assistant.chat("Hello! My name is Ron.");
        System.out.println(answer); // Hello Klaus! How can I assist you today?

        String answerWithName = assistant.chat("What is my name?");
        System.out.println(answerWithName); // Your name is Klaus.
    }
}
