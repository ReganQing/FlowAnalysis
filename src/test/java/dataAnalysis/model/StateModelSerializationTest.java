package dataAnalysis.model;

import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StateModelSerializationTest {

    @Test
    void everyDomainObjectStoredInAnalysisStateIsSerializable() {
        assertAll(
            () -> assertSerializable(DataProfile.class),
            () -> assertSerializable(ColumnProfile.class),
            () -> assertSerializable(AnalysisPlan.class),
            () -> assertSerializable(AnalysisTask.class),
            () -> assertSerializable(Insight.class),
            () -> assertSerializable(ChartEmbed.class)
        );
    }

    private static void assertSerializable(Class<?> type) {
        assertTrue(Serializable.class.isAssignableFrom(type),
            () -> type.getSimpleName() + " must implement Serializable");
    }
}
