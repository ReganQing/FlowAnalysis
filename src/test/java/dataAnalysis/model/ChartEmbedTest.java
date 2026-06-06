package dataAnalysis.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChartEmbedTest {

    @Test
    void carriesTheGeneratedChartPathForResultPreview() {
        ChartEmbed chart = new ChartEmbed("title", "base64", "description", "output/charts/chart.png");

        assertEquals("output/charts/chart.png", chart.outputPath());
    }
}
