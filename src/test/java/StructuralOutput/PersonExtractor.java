package StructuralOutput;

import dev.langchain4j.service.SystemMessage;
import model.Person;

public interface PersonExtractor {
    @SystemMessage("你是一个专门从文本中提取人物信息的助手。")
    Person extractPersonFrom(String text);
}
