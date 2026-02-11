package dataAnalysis.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import tech.tablesaw.api.Table;
import tech.tablesaw.api.NumberColumn;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 图表生成工具 (简化版)
 */
public class ChartTools {

    private static final String OUTPUT_DIR = "output/charts/";
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    static {
        new File(OUTPUT_DIR).mkdirs();
    }

    /**
     * 生成简单的柱状图
     */
    @Tool("生成简单的柱状图")
    public String createSimpleBarChart(
            @P("图表标题") String title,
            @P("数据标签(逗号分隔)") String labels,
            @P("数据值(逗号分隔)") String values) throws Exception {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        String[] labelArray = labels.split(",");
        String[] valueArray = values.split(",");

        for (int i = 0; i < Math.min(labelArray.length, valueArray.length); i++) {
            dataset.addValue(Double.parseDouble(valueArray[i].trim()), "Value", labelArray[i].trim());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                title,
                "Category",
                "Value",
                dataset
        );

        String filename = String.format("bar_%s.png", TIMESTAMP.format(LocalDateTime.now()));
        String filepath = OUTPUT_DIR + filename;
        ChartUtils.saveChartAsPNG(new File(filepath), chart, 800, 600);

        return filepath;
    }

    /**
     * 生成饼图
     */
    @Tool("生成饼图")
    public String createSimplePieChart(
            @P("图表标题") String title,
            @P("数据标签(逗号分隔)") String labels,
            @P("数据值(逗号分隔)") String values) throws Exception {

        DefaultPieDataset dataset = new DefaultPieDataset();

        String[] labelArray = labels.split(",");
        String[] valueArray = values.split(",");

        for (int i = 0; i < Math.min(labelArray.length, valueArray.length); i++) {
            dataset.setValue(labelArray[i].trim(), Double.parseDouble(valueArray[i].trim()));
        }

        JFreeChart chart = ChartFactory.createPieChart(
                title,
                dataset
        );

        String filename = String.format("pie_%s.png", TIMESTAMP.format(LocalDateTime.now()));
        String filepath = OUTPUT_DIR + filename;
        ChartUtils.saveChartAsPNG(new File(filepath), chart, 800, 600);

        return filepath;
    }

    /**
     * 从数据表生成简单的统计图表
     */
    @Tool("从数据表生成描述性统计的图表")
    public String createStatsChart(
            @P("数据表") Table data,
            @P("数值列名") String columnName) throws Exception {

        if (!data.containsColumn(columnName)) {
            throw new IllegalArgumentException("列不存在: " + columnName);
        }

        NumberColumn col = (NumberColumn) data.column(columnName);
        double mean = col.mean();
        double median = col.median();
        double min = col.min();
        double max = col.max();

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(mean, "Statistics", "Mean");
        dataset.addValue(median, "Statistics", "Median");
        dataset.addValue(min, "Statistics", "Min");
        dataset.addValue(max, "Statistics", "Max");

        JFreeChart chart = ChartFactory.createBarChart(
                columnName + " Statistics",
                "Statistic",
                "Value",
                dataset
        );

        String filename = String.format("stats_%s.png", TIMESTAMP.format(LocalDateTime.now()));
        String filepath = OUTPUT_DIR + filename;
        ChartUtils.saveChartAsPNG(new File(filepath), chart, 800, 600);

        return filepath;
    }
}
