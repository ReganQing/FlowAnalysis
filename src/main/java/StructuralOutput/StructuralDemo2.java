package StructuralOutput;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.service.AiServices;
import model.ChatModelCreator;
import model.Person;

public class StructuralDemo2 {
    public static void main(String[] args) {
        ResponseFormat responseFormat = ResponseFormat.builder()
                .type(ResponseFormatType.JSON)
                .jsonSchema(JsonSchema.builder()
                        .build())
                .build();


        ChatModel model = ChatModelCreator.newModel();
        PersonExtractor personExtractor = AiServices.create(PersonExtractor.class, model);

        String text = "        请以JSON格式解析以下人员信息：\n" +
                "        \n" +
                "        约翰42岁了，过着独立的生活。\n" +
                "        他身高1.75米，举止自信。\n" +
                "        他目前未婚，可以自由地专注于自己的个人目标和兴趣。\n" +
                "        \n" +
                "        确保响应是有效的JSON对象格式。";


        Person person = personExtractor.extractPersonFrom(text);

        System.out.println( person);
    }
}
