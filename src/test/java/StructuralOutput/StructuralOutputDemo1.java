package StructuralOutput;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.chat.response.ChatResponse;
import model.ChatModelCreator;
import model.Person;

public class StructuralOutputDemo1 {
    public static void main(String[] args) {
        ResponseFormat responseFormat = ResponseFormat.builder()
                .type(ResponseFormatType.JSON)
                .jsonSchema(JsonSchema.builder()
                        .name("Person")
                        .rootElement(JsonObjectSchema.builder()
                                .addStringProperty("name")
                                .addIntegerProperty("age")
                                .addNumberProperty("height")
                                .addBooleanProperty("married")
                                .required("name", "age", "height", "married")
                                .build())
                        .build())
                .build();

        UserMessage userMessage = UserMessage.from("""
        请以JSON格式解析以下人员信息：
        
        约翰42岁了，过着独立的生活。
        他身高1.75米，举止自信。
        他目前未婚，可以自由地专注于自己的个人目标和兴趣。
        
        确保响应是有效的JSON对象格式。
        """);

        ChatRequest chatRequest = ChatRequest.builder()
                .responseFormat(responseFormat)
                .messages(userMessage)
                .build();

        ChatModel model = ChatModelCreator.newModel();

        ChatResponse answer = model.chat(chatRequest);

        String output = answer.aiMessage().text();
        System.out.println(output);


        try {
            Person person = new ObjectMapper().readValue(output, Person.class);
            System.out.println(person);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}



