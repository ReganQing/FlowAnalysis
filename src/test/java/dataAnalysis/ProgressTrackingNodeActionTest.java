package dataAnalysis;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressTrackingNodeActionTest {

    @Test
    void reportsStartBeforeExecutionAndCompletionAfterExecution() throws Exception {
        List<String> events = new ArrayList<>();
        long[] duration = new long[1];
        ProgressListener listener = new NoOpProgressListener() {
            @Override
            public void onNodeStart(String nodeName, int stageIndex) {
                events.add("start:" + nodeName + ":" + stageIndex);
            }

            @Override
            public void onNodeProgress(String nodeName, String message) {
                events.add("progress:" + nodeName + ":" + message);
            }

            @Override
            public void onNodeComplete(String nodeName, long durationMs) {
                events.add("complete:" + nodeName);
                duration[0] = durationMs;
            }
        };

        var action = ProgressTrackingNodeAction.wrap("parser", 1, state -> {
            events.add("execute");
            Thread.sleep(20);
            return Map.of();
        }, listener);

        action.apply(new AnalysisState(Map.of()));

        assertEquals(List.of(
            "start:parser:1",
            "progress:parser:正在执行 parser",
            "execute",
            "complete:parser"
        ), events);
        assertTrue(duration[0] >= 15, "duration should include node execution");
    }
}
