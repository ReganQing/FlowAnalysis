package dataAnalysis.model;

/**
 * 嵌入报告的图表数据
 */
public record ChartEmbed(
    String title,
    String base64Image,
    String description
) {}
