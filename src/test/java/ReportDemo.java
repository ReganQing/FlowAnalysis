import dataAnalysis.model.*;
import dataAnalysis.tools.ChartTools;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

/**
 * HTML 报告输出效果测试
 * 使用模拟数据 + 真实图表验证报告样式，无需 API Key
 */
public class ReportDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== 生成测试报告 ===\n");

        // 1. 构建数据概况
        List<ColumnProfile> columns = List.of(
            new ColumnProfile("date", "Date", 0, 90, 0.0),
            new ColumnProfile("product_name", "String", 0, 10, 0.0),
            new ColumnProfile("category", "String", 3, 4, 0.006),
            new ColumnProfile("quantity", "Number", 0, 10, 0.0),
            new ColumnProfile("amount", "Number", 12, 480, 0.024),
            new ColumnProfile("region", "String", 0, 8, 0.0),
            new ColumnProfile("sales_channel", "String", 0, 2, 0.0)
        );

        DataProfile profile = new DataProfile(
            500, 7, columns,
            List.of("quantity", "amount"),
            List.of("product_name", "category", "region", "sales_channel"),
            List.of("date")
        );

        // 2. 构建智能洞察
        List<Insight> insights = List.of(
            new Insight(
                "笔记本电脑贡献了总销售额的 42.3%",
                InsightSeverity.POSITIVE,
                "P001 笔记本电脑 amount 总计 ¥2,418,765，占全品类总额的 42.3%",
                "笔记本电脑单价最高(¥5,999)，虽然销量不是最大，但金额贡献远超其他品类",
                "维持笔记本电脑的库存水平，可考虑搭配高毛利配件做组合促销"
            ),
            new Insight(
                "杭州地区销售额环比下降 15.2%",
                InsightSeverity.WARNING,
                "杭州地区近30天销售额 ¥87,320，对比前30天 ¥102,960，下降 15.2%",
                "下降可能与促销活动结束或竞品进入有关",
                "建议调查杭州区域近期市场变化，考虑针对性促销"
            ),
            new Insight(
                "线下渠道平均客单价为线上的 2.8 倍",
                InsightSeverity.INFO,
                "线下平均 ¥2,340/笔，线上平均 ¥836/笔",
                "线下客户倾向于购买高单价商品（如笔记本电脑、显示器）",
                "优化线上高单价商品的展示和推荐策略"
            ),
            new Insight(
                "3 笔异常大额交易超过 ¥50,000",
                InsightSeverity.CRITICAL,
                "数据中存在 3 笔 amount > ¥50,000 的记录，占总记录的 0.6%",
                "可能为企业采购或数据录入错误，需人工核实",
                "建议人工核实这 3 笔交易的合法性"
            )
        );

        // 3. 用 ChartTools 生成真实图表
        ChartTools chartTools = new ChartTools();
        System.out.println("生成图表...");

        String barPath = chartTools.createSimpleBarChart(
            "区域销售分布",
            "北京,上海,广州,深圳,杭州,成都,武汉,西安",
            "312450,287630,256780,234560,195240,178920,156340,142670"
        );
        System.out.println("  柱状图: " + barPath);

        String statsPath = chartTools.createStatsChart(
            tech.tablesaw.api.Table.create("amount",
                tech.tablesaw.api.DoubleColumn.create("amount",
                    120.5, 340.0, 899.0, 1599.0, 5999.0,
                    199.0, 499.0, 1999.0, 299.0, 99.0)),
            "amount"
        );
        System.out.println("  统计图: " + statsPath);

        String piePath = chartTools.createSimplePieChart(
            "品类销售占比",
            "电子产品,配件",
            "1523400,452890"
        );
        System.out.println("  饼图: " + piePath);

        String linePath = chartTools.createLineChart(
            "月度销售趋势",
            "3月,4月,5月,6月",
            "520000,580000,610000,540000",
            false
        );
        System.out.println("  折线图: " + linePath);

        // 编码为 Base64
        List<ChartEmbed> charts = List.of(
            new ChartEmbed("区域销售分布", encodeToBase64(barPath), "各区域销售额对比"),
            new ChartEmbed("品类销售占比", encodeToBase64(piePath), "电子产品 vs 配件"),
            new ChartEmbed("月度销售趋势", encodeToBase64(linePath), "近4个月销售额走势"),
            new ChartEmbed("销售额统计", encodeToBase64(statsPath), "均值、中位数、最小值、最大值")
        );

        // 4. 构建分析章节
        List<ReportData.AnalysisSection> sections = List.of(
            new ReportData.AnalysisSection(
                "区域销售分析",
                "按区域汇总销售额:\n" +
                "  • 北京: ¥312,450 (15.8%)\n" +
                "  • 上海: ¥287,630 (14.5%)\n" +
                "  • 广州: ¥256,780 (13.0%)\n" +
                "  • 深圳: ¥234,560 (11.9%)\n" +
                "  • 杭州: ¥195,240 (9.9%)\n" +
                "  • 成都: ¥178,920 (9.1%)\n" +
                "  • 武汉: ¥156,340 (7.9%)\n" +
                "  • 西安: ¥142,670 (7.2%)\n\n" +
                "北京和上海合计贡献 30.3% 的销售额，为第一梯队。",
                "区域销售分析"
            ),
            new ReportData.AnalysisSection(
                "品类与渠道分析",
                "各品类销售贡献:\n" +
                "  • 电子产品: ¥1,523,400 (77.1%)\n" +
                "  • 配件: ¥452,890 (22.9%)\n\n" +
                "渠道对比:\n" +
                "  • 线下: ¥1,340,000 (67.8%) — 平均客单价 ¥2,340\n" +
                "  • 线上: ¥636,290 (32.2%) — 平均客单价 ¥836\n\n" +
                "电子产品中笔记本电脑单品贡献最高，配件品类中充电宝和电脑包表现较好。",
                "品类与渠道分析"
            )
        );

        // 5. 构建建议
        List<String> recommendations = List.of(
            "维持笔记本电脑库存，搭配配件做组合营销提升客单价",
            "调查杭州区域销售下降原因，制定针对性促销计划",
            "优化线上高单价商品的推荐算法，缩小线上线下客单价差距",
            "核实 3 笔异常大额交易（> ¥50,000），排除数据错误",
            "考虑在成都、武汉、西安等低渗透区域加大推广投入"
        );

        // 6. 组装并生成
        ReportData reportData = new ReportData(
            profile,
            insights,
            charts,
            sections,
            recommendations,
            "2026-06-06 00:30:00",
            "sample_data/sales_data_sample.csv"
        );

        String reportPath = dataAnalysis.report.HtmlReportGenerator.generate(reportData);

        System.out.println("\n报告已生成: " + reportPath);
        System.out.println("请在浏览器中打开查看效果。");

        // 尝试自动打开浏览器
        try {
            java.awt.Desktop.getDesktop().browse(Path.of(reportPath).toAbsolutePath().toUri());
            System.out.println("已自动打开浏览器。");
        } catch (Exception e) {
            System.out.println("自动打开失败，请手动打开: " +
                Path.of(reportPath).toAbsolutePath());
        }
    }

    private static String encodeToBase64(String filePath) throws Exception {
        byte[] bytes = Files.readAllBytes(Path.of(filePath));
        return Base64.getEncoder().encodeToString(bytes);
    }
}
