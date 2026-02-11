package dataAnalysis;

import java.util.*;

/**
 * 数据分析工作流状态管理类
 * 用于在各个智能体之间传递状态和数据
 */
public class AnalysisState {

    // 状态键定义
    public static final String CSV_PATH_KEY = "csv_path";
    public static final String RAW_DATA_KEY = "raw_data";
    public static final String CLEANED_DATA_KEY = "cleaned_data";
    public static final String ANALYSIS_RESULT_KEY = "analysis_result";
    public static final String CHART_PATHS_KEY = "chart_paths";
    public static final String REPORT_PATH_KEY = "report_path";
    public static final String ERRORS_KEY = "errors";
    public static final String CURRENT_STEP_KEY = "current_step";
    public static final String DATA_SUMMARY_KEY = "data_summary";

    private final Map<String, Object> data;

    public AnalysisState(Map<String, Object> initData) {
        this.data = new HashMap<>(initData != null ? initData : Map.of());
    }

    // 便捷访问方法

    public String csvPath() {
        return get(CSV_PATH_KEY, "");
    }

    public Map<String, Object> rawData() {
        return get(RAW_DATA_KEY, Map.of());
    }

    public Map<String, Object> cleanedData() {
        return get(CLEANED_DATA_KEY, Map.of());
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> analysisResult() {
        Object value = data.get(ANALYSIS_RESULT_KEY);
        if (value instanceof Map) {
            return (Map<String, String>) value;
        }
        return Map.of();
    }

    @SuppressWarnings("unchecked")
    public List<String> chartPaths() {
        Object value = data.get(CHART_PATHS_KEY);
        if (value instanceof List) {
            return (List<String>) value;
        }
        return List.of();
    }

    public String reportPath() {
        return get(REPORT_PATH_KEY, "");
    }

    @SuppressWarnings("unchecked")
    public List<String> errors() {
        Object value = data.get(ERRORS_KEY);
        if (value instanceof List) {
            return (List<String>) value;
        }
        return List.of();
    }

    public String currentStep() {
        return get(CURRENT_STEP_KEY, "START");
    }

    public String dataSummary() {
        return get(DATA_SUMMARY_KEY, "");
    }

    // 通用获取方法
    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        Object value = data.get(key);
        if (value == null) return defaultValue;
        try {
            return (T) value;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public Object get(String key) {
        return data.get(key);
    }

    public Object getOrDefault(String key, Object defaultValue) {
        return data.getOrDefault(key, defaultValue);
    }
}
