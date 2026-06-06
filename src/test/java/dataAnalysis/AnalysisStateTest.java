package dataAnalysis;

import org.bsc.langgraph4j.state.AgentStateFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class AnalysisStateTest {

    @Test
    void schemaCanInitializeBeforeTheFirstGraphNode() {
        AgentStateFactory<AnalysisState> factory = AnalysisState::new;

        assertDoesNotThrow(() -> factory.initialDataFromSchema(AnalysisState.SCHEMA));
    }
}
