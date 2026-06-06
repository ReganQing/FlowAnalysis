package desktop.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TitleServiceTest {

    @Test
    void generatesCleanShortTitleWithLightweightModel() {
        TitleService service = new TitleService(prompt -> "《JavaFX 流式聊天问题排查》\n");

        String title = service.generateTitle("为什么聊天没有流式回复？", "因为网络请求运行在 UI 线程。");

        assertEquals("JavaFX 流式聊天问题排查", title);
    }

    @Test
    void fallsBackToUserMessageWhenModelFails() {
        TitleService service = new TitleService(prompt -> {
            throw new RuntimeException("model unavailable");
        });

        String title = service.generateTitle("这是一个超过二十个字符的用户问题，用来验证回退标题", "回答");

        assertEquals("这是一个超过二十个字符的用户问题，用来验...", title);
    }
}
