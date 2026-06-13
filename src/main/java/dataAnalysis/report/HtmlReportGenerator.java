package dataAnalysis.report;

import dataAnalysis.model.ChartEmbed;
import dataAnalysis.model.DataProfile;
import dataAnalysis.model.Insight;
import dataAnalysis.model.ReportData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * HTML 报告生成器
 * 生成单文件 HTML 报告，CSS + 图表全部内嵌
 *
 * 设计原则：清晰简洁、高贵优雅、克制用色
 * 禁止蓝绿渐变、红紫渐变
 */
public final class HtmlReportGenerator {

    private static final String OUTPUT_DIR = "output/reports/";
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static {
        new File(OUTPUT_DIR).mkdirs();
    }

    private HtmlReportGenerator() {}

    /**
     * 生成 HTML 报告并保存到文件
     *
     * @param reportData 报告数据
     * @return 生成的 HTML 文件路径
     */
    public static String generate(ReportData reportData) throws IOException {
        String html = buildHtml(reportData);
        String filename = String.format("analysis_report_%s.html", TIMESTAMP.format(LocalDateTime.now()));
        File outputFile = new File(OUTPUT_DIR, filename).getAbsoluteFile();
        String filepath = outputFile.getAbsolutePath();

        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(html);
        }
        return filepath;
    }

    /**
     * 构建 HTML 报告内容
     */
    static String buildHtml(ReportData data) {
        StringBuilder sb = new StringBuilder(8192);
        appendHtmlHeader(sb);
        sb.append("<body>\n");

        // 报告头
        appendReportHeader(sb, data);

        // KPI 概览卡片
        appendKpiSection(sb, data.profile());

        // AI 执行摘要
        appendExecutiveSummary(sb, data);

        // AI 洞察
        appendInsightsSection(sb, data.insights());

        // 分析章节
        appendAnalysisSections(sb, data.analysisSections(), data.charts());

        // 业务建议
        appendRecommendations(sb, data.recommendations());

        // 页脚
        appendFooter(sb);

        sb.append("</body>\n</html>");
        return sb.toString();
    }

    // ========== HTML 头部 ==========

    private static void appendHtmlHeader(StringBuilder sb) {
        sb.append("""
            <!DOCTYPE html>
            <html lang="zh-CN">
            <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>数据分析报告</title>
            <style>
              :root {
                --bg-primary: #FAFAFA;
                --bg-card: #FFFFFF;
                --text-primary: #1A1A1A;
                --text-secondary: #6B6B6B;
                --divider: #E0E0E0;
                --accent: #22D3EE;
                --critical: #B85450;
                --warning: #C89B3C;
                --info: #5A7A9A;
                --positive: #5A8A6A;
              }

              * { margin: 0; padding: 0; box-sizing: border-box; }

              body {
                font-family: 'Microsoft YaHei', 'PingFang SC', -apple-system, sans-serif;
                background-color: var(--bg-primary);
                color: var(--text-primary);
                line-height: 1.7;
                padding: 0 0 48px 0;
              }

              .container {
                max-width: 960px;
                margin: 0 auto;
                padding: 0 32px;
              }

              /* --- 报告头 --- */
              .report-header {
                padding: 48px 0 24px 0;
                border-bottom: 1px solid var(--divider);
                margin-bottom: 48px;
              }
              .report-header h1 {
                font-size: 24px;
                font-weight: 600;
                color: var(--text-primary);
                margin-bottom: 8px;
              }
              .report-header .meta {
                font-size: 13px;
                color: var(--text-secondary);
              }
              .report-header .meta span { margin-right: 24px; }
              .report-header .accent-line {
                width: 40px;
                height: 2px;
                background-color: var(--accent);
                margin-top: 16px;
              }

              /* --- 章节标题 --- */
              .section-title {
                font-size: 18px;
                font-weight: 600;
                color: var(--text-primary);
                padding-bottom: 12px;
                border-bottom: 1px solid var(--divider);
                margin-bottom: 24px;
              }

              /* --- KPI 卡片网格 --- */
              .kpi-grid {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
                gap: 16px;
                margin-bottom: 48px;
              }

              /* --- 执行摘要 --- */
              .executive-summary {
                background: var(--bg-card);
                padding: 24px;
                border-left: 3px solid var(--accent);
                border-bottom: 1px solid var(--divider);
                font-size: 15px;
                line-height: 1.9;
                color: var(--text-primary);
                margin-bottom: 48px;
              }

              .kpi-card {
                background: var(--bg-card);
                padding: 24px;
                border-bottom: 1px solid var(--divider);
              }
              .kpi-card .value {
                font-family: 'Georgia', serif;
                font-size: 32px;
                font-weight: 400;
                color: var(--text-primary);
                margin-bottom: 4px;
              }
              .kpi-card .label {
                font-size: 13px;
                color: var(--text-secondary);
              }

              /* --- 洞察卡片 --- */
              .insights-list { margin-bottom: 48px; }
              .insight-item {
                background: var(--bg-card);
                padding: 16px 20px;
                border-left: 3px solid var(--accent);
                border-bottom: 1px solid var(--divider);
                margin-bottom: 2px;
              }
              .insight-item.critical { border-left-color: var(--critical); }
              .insight-item.warning  { border-left-color: var(--warning); }
              .insight-item.info     { border-left-color: var(--info); }
              .insight-item.positive { border-left-color: var(--positive); }
              .insight-item .insight-title {
                font-size: 15px;
                font-weight: 600;
                margin-bottom: 6px;
              }
              .insight-item .insight-severity {
                font-size: 12px;
                padding: 2px 8px;
                border-radius: 2px;
                margin-left: 8px;
                vertical-align: middle;
              }
              .insight-severity.critical { background: var(--critical); color: #fff; }
              .insight-severity.warning  { background: var(--warning); color: #fff; }
              .insight-severity.info     { background: var(--info); color: #fff; }
              .insight-severity.positive { background: var(--positive); color: #fff; }
              .insight-item .insight-body {
                font-size: 13px;
                color: var(--text-secondary);
                line-height: 1.8;
              }
              .insight-item .insight-evidence {
                margin-top: 4px;
              }
              .insight-item .insight-recommendation {
                margin-top: 4px;
                color: var(--text-primary);
              }

              /* --- 分析章节 --- */
              .analysis-section {
                margin-bottom: 48px;
              }
              .analysis-section h3 {
                font-size: 16px;
                font-weight: 600;
                margin-bottom: 16px;
              }
              .analysis-section .content {
                font-size: 14px;
                color: var(--text-secondary);
                line-height: 1.8;
                margin-bottom: 16px;
                white-space: pre-wrap;
              }
              .analysis-section .chart-container {
                text-align: center;
                margin: 16px 0;
              }
              .analysis-section .chart-container img {
                max-width: 100%;
                height: auto;
                border-bottom: 1px solid var(--divider);
              }
              .analysis-section .chart-caption {
                font-size: 13px;
                color: var(--text-secondary);
                margin-top: 8px;
              }

              /* --- 建议 --- */
              .recommendations {
                margin-bottom: 48px;
              }
              .recommendations ol {
                padding-left: 24px;
              }
              .recommendations li {
                font-size: 14px;
                color: var(--text-secondary);
                line-height: 2;
                border-bottom: 1px solid var(--divider);
                padding: 4px 0;
              }
              .recommendations li:last-child { border-bottom: none; }

              /* --- 页脚 --- */
              .report-footer {
                padding-top: 24px;
                border-top: 1px solid var(--divider);
                text-align: center;
                font-size: 12px;
                color: var(--text-secondary);
              }

              /* --- 打印样式 --- */
              @media print {
                body { background: #fff; }
                .kpi-card, .insight-item { break-inside: avoid; }
                .chart-container img { max-width: 600px; }
              }
            </style>
            </head>
            """);
    }

    // ========== 报告头 ==========

    private static void appendReportHeader(StringBuilder sb, ReportData data) {
        sb.append("<div class=\"container\">\n");
        sb.append("<div class=\"report-header\">\n");
        sb.append("  <h1>数据分析报告</h1>\n");
        sb.append("  <div class=\"meta\">\n");
        sb.append(String.format("    <span>生成时间: %s</span>\n", escapeHtml(data.generatedAt())));
        sb.append(String.format("    <span>数据源: %s</span>\n", escapeHtml(data.dataSource())));
        sb.append("  </div>\n");
        sb.append("  <div class=\"accent-line\"></div>\n");
        sb.append("</div>\n");
    }

    // ========== KPI 概览 ==========

    private static void appendKpiSection(StringBuilder sb, DataProfile profile) {
        if (profile == null) return;

        sb.append("<h2 class=\"section-title\">数据概览</h2>\n");
        sb.append("<div class=\"kpi-grid\">\n");

        appendKpiCard(sb, formatNumber(profile.rowCount()), "数据记录");
        appendKpiCard(sb, formatNumber(profile.columnCount()), "字段数");
        appendKpiCard(sb, formatNumber(profile.numericColumns().size()), "数值字段");
        appendKpiCard(sb, formatNumber(profile.categoricalColumns().size()), "分类字段");

        sb.append("</div>\n");
    }

    // ========== 执行摘要 ==========

    private static void appendExecutiveSummary(StringBuilder sb, ReportData data) {
        String summary = data.executiveSummary();
        if (summary == null || summary.isBlank()) return;

        sb.append("<h2 class=\"section-title\">执行摘要</h2>\n");
        sb.append("<div class=\"executive-summary\">\n");
        sb.append(escapeHtml(summary));
        sb.append("\n</div>\n");
    }

    private static void appendKpiCard(StringBuilder sb, String value, String label) {
        sb.append("  <div class=\"kpi-card\">\n");
        sb.append(String.format("    <div class=\"value\">%s</div>\n", escapeHtml(value)));
        sb.append(String.format("    <div class=\"label\">%s</div>\n", escapeHtml(label)));
        sb.append("  </div>\n");
    }

    // ========== AI 洞察 ==========

    private static void appendInsightsSection(StringBuilder sb, List<Insight> insights) {
        if (insights == null || insights.isEmpty()) return;

        sb.append("<h2 class=\"section-title\">AI 智能洞察</h2>\n");
        sb.append("<div class=\"insights-list\">\n");

        for (Insight insight : insights) {
            String severityClass = insight.severity().name().toLowerCase();
            sb.append("  <div class=\"insight-item ").append(severityClass).append("\">\n");
            sb.append("    <div class=\"insight-title\">")
              .append(escapeHtml(insight.title()))
              .append("<span class=\"insight-severity ").append(severityClass).append("\">")
              .append(escapeHtml(insight.severity().getLabel()))
              .append("</span>")
              .append("</div>\n");
            sb.append("    <div class=\"insight-body\">\n");

            if (insight.explanation() != null && !insight.explanation().isBlank()) {
                sb.append("      <div>").append(escapeHtml(insight.explanation())).append("</div>\n");
            }

            if (insight.evidence() != null && !insight.evidence().isBlank()) {
                sb.append("      <div class=\"insight-evidence\">证据: ")
                  .append(escapeHtml(insight.evidence())).append("</div>\n");
            }

            if (insight.recommendation() != null && !insight.recommendation().isBlank()) {
                sb.append("      <div class=\"insight-recommendation\">建议: ")
                  .append(escapeHtml(insight.recommendation())).append("</div>\n");
            }

            sb.append("    </div>\n");
            sb.append("  </div>\n");
        }

        sb.append("</div>\n");
    }

    // ========== 分析章节 ==========

    private static void appendAnalysisSections(StringBuilder sb,
            List<ReportData.AnalysisSection> sections,
            List<ChartEmbed> charts) {
        if (sections == null || sections.isEmpty()) return;

        sb.append("<h2 class=\"section-title\">分析详情</h2>\n");

        for (int i = 0; i < sections.size(); i++) {
            ReportData.AnalysisSection section = sections.get(i);
            sb.append("<div class=\"analysis-section\">\n");
            sb.append(String.format("  <h3>%d. %s</h3>\n", i + 1, escapeHtml(section.title())));

            if (section.displayContent() != null && !section.displayContent().isBlank()) {
                sb.append("  <div class=\"content\">").append(escapeHtml(section.displayContent())).append("</div>\n");
            }

            // 按序号关联图表
            if (charts != null && i < charts.size()) {
                appendEmbeddedChart(sb, charts.get(i));
            }

            sb.append("</div>\n");
        }

        // 多出的图表单独展示
        if (charts != null) {
            int extraStart = Math.min(charts.size(), sections.size());
            for (int i = extraStart; i < charts.size(); i++) {
                sb.append("<div class=\"analysis-section\">\n");
                sb.append(String.format("  <h3>%d. %s</h3>\n", i + 1, escapeHtml(charts.get(i).title())));
                appendEmbeddedChart(sb, charts.get(i));
                sb.append("</div>\n");
            }
        }
    }

    private static void appendEmbeddedChart(StringBuilder sb, ChartEmbed chart) {
        if (chart.base64Image() == null || chart.base64Image().isBlank()) return;

        sb.append("  <div class=\"chart-container\">\n");
        sb.append("    <img src=\"data:image/png;base64,").append(chart.base64Image()).append("\"")
          .append(" alt=\"").append(escapeHtml(chart.title())).append("\"/>\n");
        if (chart.description() != null && !chart.description().isBlank()) {
            sb.append("    <div class=\"chart-caption\">").append(escapeHtml(chart.description())).append("</div>\n");
        }
        sb.append("  </div>\n");
    }

    // ========== 业务建议 ==========

    private static void appendRecommendations(StringBuilder sb, List<String> recommendations) {
        if (recommendations == null || recommendations.isEmpty()) return;

        sb.append("<h2 class=\"section-title\">业务建议</h2>\n");
        sb.append("<div class=\"recommendations\">\n");
        sb.append("  <ol>\n");
        for (String rec : recommendations) {
            sb.append("    <li>").append(escapeHtml(rec)).append("</li>\n");
        }
        sb.append("  </ol>\n");
        sb.append("</div>\n");
    }

    // ========== 页脚 ==========

    private static void appendFooter(StringBuilder sb) {
        sb.append("<div class=\"report-footer\">\n");
        sb.append("  本报告由 LangChain4j 数据分析多智能体系统自动生成<br>\n");
        sb.append(String.format("  %s\n", LocalDateTime.now().format(DISPLAY_TIME)));
        sb.append("</div>\n");
        sb.append("</div>\n");  // close container
    }

    // ========== 工具方法 ==========

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    private static String formatNumber(int value) {
        if (value >= 1_000_000) {
            return String.format("%.1fM", value / 1_000_000.0);
        } else if (value >= 1_000) {
            return String.format("%.1fK", value / 1_000.0);
        }
        return String.valueOf(value);
    }
}
