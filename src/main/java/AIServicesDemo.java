import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.tool.ToolExecution;
import model.ChatModelCreator;

import java.util.List;

public class AIServicesDemo {
    public static void main(String[] args) {
        ChatModel model = ChatModelCreator.newModel();

        AIAssistant assistant = AiServices.create(AIAssistant.class, model);
        // System.out.println(assistant.chat("生活的意义是什么?"));

        Result<List<String>> result = assistant.generateOutlineFor("Java");

        List<String> outline = result.content();
        TokenUsage tokenUsage = result.tokenUsage();
        List<Content> sources = result.sources();
        List<ToolExecution> toolExecutions = result.toolExecutions();
        FinishReason finishReason = result.finishReason();

        System.out.println(outline);
        System.out.println(tokenUsage);
        System.out.println(sources);
        System.out.println(toolExecutions);
        System.out.println(finishReason);
    }
}
