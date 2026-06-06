package desktop.service;

import model.ChatModelCreator;

import java.util.function.Function;

public class TitleService {

    private static final String TITLE_MODEL = "qwen-turbo-latest";
    private final Function<String, String> titleGenerator;

    public TitleService() {
        this(prompt -> ChatModelCreator.newModel(TITLE_MODEL, 0.1).chat(prompt));
    }

    TitleService(Function<String, String> titleGenerator) {
        this.titleGenerator = titleGenerator;
    }

    public String generateTitle(String userMessage, String aiResponse) {
        try {
            String prompt = """
                请根据下面的用户问题和 AI 回答生成一个简洁的中文会话标题。
                只输出标题，不要引号、书名号、句号或解释，最多 20 个字符。

                用户问题：%s
                AI 回答：%s
                """.formatted(userMessage, aiResponse);
            String generated = clean(titleGenerator.apply(prompt));
            return generated.isBlank() ? fallback(userMessage) : generated;
        } catch (RuntimeException e) {
            return fallback(userMessage);
        }
    }

    private String clean(String title) {
        if (title == null) return "";
        String cleaned = title.strip()
            .replaceAll("^[《“\"']+", "")
            .replaceAll("[》”\"'。]+$", "")
            .replaceAll("\\R.*", "")
            .strip();
        return cleaned.length() > 20 ? cleaned.substring(0, 20) : cleaned;
    }

    private String fallback(String userMessage) {
        String cleaned = userMessage == null ? "" : userMessage.strip();
        return cleaned.length() > 20 ? cleaned.substring(0, 20) + "..." : cleaned;
    }
}
