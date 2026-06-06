package dataAnalysis;

import dataAnalysis.model.AnalysisPlan;
import dataAnalysis.model.ChartEmbed;
import dataAnalysis.model.DataProfile;
import dataAnalysis.model.Insight;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据分析工作流状态
 * 基于LangGraph4J AgentState，在各节点间安全传递数据
 */
public class AnalysisState extends AgentState {

    public static final String CSV_PATH_KEY = "csv_path";
    public static final String RAW_DATA_KEY = "raw_data";
    public static final String CLEANED_DATA_KEY = "cleaned_data";
    public static final String DATA_PROFILE_KEY = "data_profile";
    public static final String ANALYSIS_PLAN_KEY = "analysis_plan";
    public static final String ANALYSIS_RESULTS_KEY = "analysis_results";
    public static final String INSIGHTS_KEY = "insights";
    public static final String CHART_EMBEDS_KEY = "chart_embeds";
    public static final String REPORT_PATH_KEY = "report_path";
    public static final String ERRORS_KEY = "errors";
    public static final String CURRENT_STEP_KEY = "current_step";
    public static final String DATA_SUMMARY_KEY = "data_summary";

    /**
     * LangGraph4J Channel Schema
     */
    public static final Map<String, Channel<?>> SCHEMA = Map.ofEntries(
        Map.entry(CSV_PATH_KEY,           lastValue()),
        Map.entry(RAW_DATA_KEY,           lastValue()),
        Map.entry(CLEANED_DATA_KEY,       lastValue()),
        Map.entry(DATA_PROFILE_KEY,       lastValue()),
        Map.entry(ANALYSIS_PLAN_KEY,      lastValue()),
        Map.entry(ANALYSIS_RESULTS_KEY,   Channels.appender(ArrayList::new)),
        Map.entry(INSIGHTS_KEY,           Channels.appender(ArrayList::new)),
        Map.entry(CHART_EMBEDS_KEY,       Channels.appender(ArrayList::new)),
        Map.entry(REPORT_PATH_KEY,        lastValue()),
        Map.entry(ERRORS_KEY,             Channels.appender(ArrayList::new)),
        Map.entry(CURRENT_STEP_KEY,       Channels.base(() -> "START")),
        Map.entry(DATA_SUMMARY_KEY,       Channels.base(() -> ""))
    );

    private static <T> Channel<T> lastValue() {
        return Channels.base((oldValue, newValue) -> newValue);
    }

    public AnalysisState(Map<String, Object> initData) {
        super(initData);
    }

    public String csvPath() {
        return value(CSV_PATH_KEY).orElse("").toString();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> rawData() {
        return value(RAW_DATA_KEY).map(d -> (Map<String, Object>) d).orElse(Map.of());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> cleanedData() {
        return value(CLEANED_DATA_KEY).map(d -> (Map<String, Object>) d).orElse(Map.of());
    }

    public DataProfile dataProfile() {
        return value(DATA_PROFILE_KEY).map(d -> (DataProfile) d).orElse(null);
    }

    public AnalysisPlan analysisPlan() {
        return value(ANALYSIS_PLAN_KEY).map(d -> (AnalysisPlan) d).orElse(null);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, String>> analysisResults() {
        return value(ANALYSIS_RESULTS_KEY).map(d -> (List<Map<String, String>>) d).orElse(List.of());
    }

    @SuppressWarnings("unchecked")
    public List<Insight> insights() {
        return value(INSIGHTS_KEY).map(d -> (List<Insight>) d).orElse(List.of());
    }

    @SuppressWarnings("unchecked")
    public List<ChartEmbed> chartEmbeds() {
        return value(CHART_EMBEDS_KEY).map(d -> (List<ChartEmbed>) d).orElse(List.of());
    }

    public String reportPath() {
        return value(REPORT_PATH_KEY).orElse("").toString();
    }

    @SuppressWarnings("unchecked")
    public List<String> errors() {
        return value(ERRORS_KEY).map(d -> (List<String>) d).orElse(List.of());
    }

    public String currentStep() {
        return value(CURRENT_STEP_KEY).orElse("START").toString();
    }

    public String dataSummary() {
        return value(DATA_SUMMARY_KEY).orElse("").toString();
    }
}
