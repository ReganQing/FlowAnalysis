import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import model.ChatModelCreator;

public class SentimentClassification {
    public static void main(String[] args) {
        // 1.聊天模型初始化
        ChatModel model = ChatModelCreator.newModel();

        // 2.定义情感类别
        enum Sentiment {
            POSITIVE, NEGATIVE, NEUTRAL
        }

        // 3.创建AI驱动的情感分析器
        interface SentimentClassifier {
            @UserMessage("请对 {{it}} 进行情感分析")
            Sentiment analyzeSentimentOf(String text);

            @UserMessage("{{it}} 是否有一个积极情绪")
            boolean isPositive(String text);
        }

        // 4.创建AI服务实例
        SentimentClassifier sentimentClassifier = AiServices.create(SentimentClassifier.class, model);

        // 5.运行情感分析
        System.out.println(sentimentClassifier.analyzeSentimentOf("我非常开心"));
        System.out.println(sentimentClassifier.isPositive("我非常开心"));
        System.out.println(sentimentClassifier.isPositive("今天我过得非常糟糕"));
    }
}
