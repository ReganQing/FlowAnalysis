import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

import java.util.List;

public interface AIAssistant {
    @SystemMessage("You are a helpful assistant.")
    String chat(@UserMessage String message);

    @UserMessage("Generate an outline for the article on the following topic: {{it}}")
    Result<List<String>> generateOutlineFor(String topic);

    TokenStream streamingChat(String message);
}
