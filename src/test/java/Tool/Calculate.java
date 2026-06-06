package Tool;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import model.ChatModelCreator;

public class Calculate {
    public static void main(String[] args) {
        OpenAiChatModel openAiChatModel = ChatModelCreator.newModel();

        MathGenius mathGenius = AiServices.builder(MathGenius.class)
                .chatModel(openAiChatModel)
                .tools(new Calculator())
                .build();

        String answer1 = mathGenius.ask("2+2");
        System.out.println(answer1);
        String answer2 = mathGenius.ask("475695037565 的平方根是多少？");
        System.out.println(answer2);
    }
}
