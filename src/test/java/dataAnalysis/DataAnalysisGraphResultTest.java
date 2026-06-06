package dataAnalysis;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DataAnalysisGraphResultTest {

    @Test
    void rejectsPipelineResultWhenReportWasNotGenerated() {
        AnalysisState state = new AnalysisState(Map.of(
            AnalysisState.REPORT_PATH_KEY, "",
            AnalysisState.ERRORS_KEY, java.util.List.of("report failed")
        ));

        assertThrows(IllegalStateException.class,
            () -> DataAnalysisGraph.createResult("input.csv", state));
    }
}
