import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import model.ChatModelCreator;

public class StreamingChatDemo2 {
    public static void main(String[] args) {

        StreamingChatModel model = ChatModelCreator.newStreamingModel();

        AIAssistant assistant = AiServices.create(AIAssistant.class, model);

        TokenStream tokenStream = assistant.streamingChat("给我讲一个关于爬山的笑话");

        tokenStream.onPartialResponse(System.out::println)
                .onRetrieved(System.out::println)
                .onToolExecuted(System.out::println)
                .onCompleteResponse(System.out::println)
                .onError(Throwable::printStackTrace)
                .start();
    }
}
