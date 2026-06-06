package dataAnalysis.model;

/**
 * AI生成的智能洞察
 * 包含发现、证据、解释和建议
 */
public record Insight(
    String title,
    InsightSeverity severity,
    String evidence,
    String explanation,
    String recommendation
) implements java.io.Serializable {}
