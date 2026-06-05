package dataAnalysis.processing;

import dataAnalysis.model.DataProfile;
import dataAnalysis.model.ProcessingStrategy;

/**
 * Token预算管理器
 * 控制送入LLM的数据量，确保不超出上下文窗口
 */
public class TokenBudgetManager {

    private static final int DEFAULT_MAX_TOKENS = 4000;
    private static final int CHARS_PER_TOKEN = 4;

    private final int maxTokensForData;

    public TokenBudgetManager() {
        this(DEFAULT_MAX_TOKENS);
    }

    public TokenBudgetManager(int maxTokensForData) {
        this.maxTokensForData = maxTokensForData;
    }

    public ProcessingStrategy selectStrategy(DataProfile profile) {
        int rowCount = profile.rowCount();
        if (rowCount < 1_000) {
            return ProcessingStrategy.FULL_DATA;
        } else if (rowCount < 50_000) {
            return ProcessingStrategy.SAMPLE_AND_AGGREGATE;
        } else {
            return ProcessingStrategy.AGGREGATE_ONLY;
        }
    }

    public int estimateTokens(String text) {
        if (text == null || text.isEmpty()) return 0;
        return text.length() / CHARS_PER_TOKEN;
    }

    public boolean withinBudget(String text) {
        return estimateTokens(text) <= maxTokensForData;
    }

    public String truncateToBudget(String text) {
        int maxChars = maxTokensForData * CHARS_PER_TOKEN;
        if (text == null || text.length() <= maxChars) return text;
        return text.substring(0, maxChars) + "\n... (数据已截断以适应token预算)";
    }

    public int getMaxTokensForData() {
        return maxTokensForData;
    }
}
