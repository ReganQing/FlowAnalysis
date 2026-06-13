package dataAnalysis.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 报告生成工具
 * 提供 Markdown 报告生成功能
 */
public class ReportTools {

    private static final String OUTPUT_DIR = "output/reports/";
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    static {
        // 确保输出目录存在
        new File(OUTPUT_DIR).mkdirs();
    }

    /**
     * 生成 Markdown 报告
     */
    @Tool("生成数据分析报告的 Markdown 格式")
    public String generateMarkdownReport(
            @P("分析结果 JSON 字符串") String analysisResult,
            @P("图表路径列表") List<String> chartPaths,
            @P("数据摘要") String dataSummary) throws IOException {

        StringBuilder report = new StringBuilder();

        // 标题
        report.append("# 数据分析报告\n\n");
        report.append(String.format("**生成时间**: %s\n\n", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));

        // 数据摘要
        report.append("## 1. 数据概况\n\n");
        report.append(dataSummary).append("\n\n");

        // 分析结果
        report.append("## 2. 分析结果\n\n");
        report.append("```\n");
        report.append(analysisResult);
        report.append("\n```\n\n");

        // 图表
        if (chartPaths != null && !chartPaths.isEmpty()) {
            report.append("## 3. 数据可视化\n\n");
            for (int i = 0; i < chartPaths.size(); i++) {
                report.append(String.format("### 图表 %d\n\n", i + 1));
                report.append(String.format("![%s](%s)\n\n", "Chart " + (i + 1), chartPaths.get(i)));
            }
        }

        // 结论
        report.append("## 4. 结论与建议\n\n");
        report.append("*分析结论由 AI 助手生成*\n\n");

        // 保存报告
        String filename = String.format("analysis_report_%s.md", TIMESTAMP.format(LocalDateTime.now()));
        File outputFile = new File(OUTPUT_DIR, filename).getAbsoluteFile();

        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(report.toString());
        }

        return outputFile.getAbsolutePath();
    }

    /**
     * 将 JSON 结果转换为 Markdown 表格
     */
    @Tool("将 JSON 格式的分析结果转换为 Markdown 表格")
    public String jsonToMarkdownTable(@P("JSON 结果字符串") String jsonData) {
        // 简化实现，假设输入是特定格式
        // 实际需要使用 Jackson 或其他 JSON 库解析

        StringBuilder table = new StringBuilder();
        table.append("| 指标 | 值 |\n");
        table.append("|------|------|\n");

        // 这里需要解析 JSON 并生成表格
        // 暂时返回示例

        return table.toString();
    }

    /**
     * 追加内容到现有报告
     */
    @Tool("在现有报告末尾追加内容")
    public String appendToReport(
            @P("报告文件路径") String reportPath,
            @P("要追加的内容") String content) throws IOException {

        try (FileWriter writer = new FileWriter(reportPath, true)) {
            writer.write("\n\n");
            writer.write(content);
        }

        return reportPath;
    }
}
