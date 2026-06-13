package dataAnalysis.tools;

import dataAnalysis.chart.ChartStyle;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import tech.tablesaw.api.Table;
import tech.tablesaw.api.NumberColumn;

import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 图表生成工具
 * 支持7种图表类型，统一专业风格
 */
public class ChartTools {

    private static final String OUTPUT_DIR = "output/charts/";
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    static {
        new File(OUTPUT_DIR).mkdirs();
    }

    // ========== 柱状图 ==========

    @Tool("生成柱状图")
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

        JFreeChart chart = ChartFactory.createBarChart(title, "Category", "Value", dataset);
        var renderer = (org.jfree.chart.renderer.category.BarRenderer) chart.getCategoryPlot().getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        for (int i = 0; i < dataset.getRowCount(); i++) {
            renderer.setSeriesPaint(i, ChartStyle.color(i));
        }
        ChartStyle.applyStyle(chart);
        return saveChart(chart, "bar");
    }

    // ========== 饼图 ==========

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

        JFreeChart chart = ChartFactory.createPieChart(title, dataset);
        chart.getLegend().setVisible(true);
        ChartStyle.applyStyle(chart);
        return saveChart(chart, "pie");
    }

    // ========== 统计图 ==========

    @Tool("从数据表生成描述性统计的图表")
    public String createStatsChart(
            @P("数据表") Table data,
            @P("数值列名") String columnName) throws Exception {

        if (!data.containsColumn(columnName)) {
            throw new IllegalArgumentException("列不存在: " + columnName);
        }
        NumberColumn col = (NumberColumn) data.column(columnName);
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(col.mean(), "Statistics", "Mean");
        dataset.addValue(col.median(), "Statistics", "Median");
        dataset.addValue(col.min(), "Statistics", "Min");
        dataset.addValue(col.max(), "Statistics", "Max");

        JFreeChart chart = ChartFactory.createBarChart(columnName + " Statistics", "Statistic", "Value", dataset);
        ChartStyle.applyStyle(chart);
        return saveChart(chart, "stats");
    }

    // ========== 折线图 ==========

    @Tool("生成折线图，用于展示时间趋势或连续变化")
    public String createLineChart(
            @P("图表标题") String title,
            @P("数据标签(逗号分隔)") String labels,
            @P("数据值(逗号分隔)") String values,
            @P("是否平滑曲线") boolean smooth) throws Exception {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String[] labelArray = labels.split(",");
        String[] valueArray = values.split(",");
        for (int i = 0; i < Math.min(labelArray.length, valueArray.length); i++) {
            dataset.addValue(Double.parseDouble(valueArray[i].trim()), "Value", labelArray[i].trim());
        }

        JFreeChart chart = ChartFactory.createLineChart(title, "Category", "Value", dataset,
                PlotOrientation.VERTICAL, true, true, false);
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setSeriesPaint(0, ChartStyle.ACCENT);
        renderer.setSeriesStroke(0, new BasicStroke(2.5f));
        renderer.setDefaultItemLabelsVisible(false);
        chart.getCategoryPlot().setRenderer(renderer);
        ChartStyle.applyStyle(chart);
        return saveChart(chart, "line");
    }

    // ========== 多系列折线图 ==========

    @Tool("生成多系列折线图，用于对比多个指标的趋势")
    public String createMultiLineChart(
            @P("图表标题") String title,
            @P("X轴标签(逗号分隔)") String labels,
            @P("系列名(逗号分隔)") String seriesNames,
            @P("各系列数据(分号分隔系列)") String seriesData) throws Exception {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String[] labelArray = labels.split(",");
        String[] nameArray = seriesNames.split(",");
        String[] dataBlocks = seriesData.split(";");
        for (int s = 0; s < Math.min(nameArray.length, dataBlocks.length); s++) {
            String[] valueArray = dataBlocks[s].trim().split(",");
            for (int i = 0; i < Math.min(labelArray.length, valueArray.length); i++) {
                dataset.addValue(Double.parseDouble(valueArray[i].trim()), nameArray[s].trim(), labelArray[i].trim());
            }
        }

        JFreeChart chart = ChartFactory.createLineChart(title, "Category", "Value", dataset,
                PlotOrientation.VERTICAL, true, true, false);
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        for (int i = 0; i < dataset.getRowCount(); i++) {
            renderer.setSeriesPaint(i, ChartStyle.color(i));
            renderer.setSeriesStroke(i, new BasicStroke(2.0f));
            renderer.setDefaultItemLabelsVisible(false);
        }
        chart.getCategoryPlot().setRenderer(renderer);
        ChartStyle.applyStyle(chart);
        return saveChart(chart, "multiline");
    }

    // ========== 散点图 ==========

    @Tool("生成散点图，用于展示两个变量的关系")
    public String createScatterChart(
            @P("图表标题") String title,
            @P("X轴值(逗号分隔)") String xValues,
            @P("Y轴值(逗号分隔)") String yValues) throws Exception {

        XYSeries series = new XYSeries("Data");
        String[] xArr = xValues.split(",");
        String[] yArr = yValues.split(",");
        for (int i = 0; i < Math.min(xArr.length, yArr.length); i++) {
            try {
                series.add(Double.parseDouble(xArr[i].trim()), Double.parseDouble(yArr[i].trim()));
            } catch (NumberFormatException ignored) {}
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createScatterPlot(title, "X", "Y", dataset);
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, ChartStyle.ACCENT);
        renderer.setDefaultItemLabelsVisible(false);
        chart.getXYPlot().setRenderer(renderer);
        ChartStyle.applyStyle(chart);
        return saveChart(chart, "scatter");
    }

    // ========== 组合图 ==========

    @Tool("生成组合图，在同一图表中展示柱状和折线混合类型")
    public String createComboChart(
            @P("图表标题") String title,
            @P("X轴标签(逗号分隔)") String labels,
            @P("柱状图数据值(逗号分隔)") String barValues,
            @P("折线图数据值(逗号分隔)") String lineValues,
            @P("柱状图系列名") String barSeriesName,
            @P("折线图系列名") String lineSeriesName) throws Exception {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String[] labelArray = labels.split(",");
        String[] barArr = barValues.split(",");
        String[] lineArr = lineValues.split(",");
        for (int i = 0; i < Math.min(labelArray.length, Math.max(barArr.length, lineArr.length)); i++) {
            if (i < barArr.length) dataset.addValue(Double.parseDouble(barArr[i].trim()), barSeriesName, labelArray[i].trim());
            if (i < lineArr.length) dataset.addValue(Double.parseDouble(lineArr[i].trim()), lineSeriesName, labelArray[i].trim());
        }

        JFreeChart chart = ChartFactory.createBarChart(title, "Category", "Value", dataset,
                PlotOrientation.VERTICAL, true, true, false);
        var plot = chart.getCategoryPlot();
        NumberAxis axis2 = new NumberAxis(lineSeriesName);
        plot.setRangeAxis(1, axis2);
        plot.setDataset(1, dataset);
        plot.mapDatasetToRangeAxis(1, 1);
        LineAndShapeRenderer lineRenderer = new LineAndShapeRenderer();
        lineRenderer.setSeriesPaint(0, ChartStyle.ACCENT);
        lineRenderer.setSeriesStroke(0, new BasicStroke(2.5f));
        lineRenderer.setDefaultItemLabelsVisible(false);
        plot.setRenderer(1, lineRenderer);
        ChartStyle.applyStyle(chart);
        return saveChart(chart, "combo");
    }

    // ========== 热力图 ==========

    @Tool("生成热力图，用散点图+颜色区分模拟热力分布")
    public String createHeatmap(
            @P("图表标题") String title,
            @P("X轴值(逗号分隔)") String xValues,
            @P("Y轴值(逗号分隔)") String yValues,
            @P("热力值(逗号分隔，0-1之间)") String heatValues) throws Exception {

        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series = new XYSeries("Heat");
        String[] xArr = xValues.split(",");
        String[] yArr = yValues.split(",");
        String[] hArr = heatValues.split(",");
        for (int i = 0; i < Math.min(xArr.length, Math.min(yArr.length, hArr.length)); i++) {
            try {
                series.add(Double.parseDouble(xArr[i].trim()), Double.parseDouble(yArr[i].trim()));
            } catch (NumberFormatException ignored) {}
        }
        dataset.addSeries(series);
        JFreeChart chart = ChartFactory.createScatterPlot(title, "X", "Y", dataset);
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setDefaultItemLabelsVisible(false);
        chart.getXYPlot().setRenderer(renderer);
        ChartStyle.applyStyle(chart);
        return saveChart(chart, "heatmap");
    }

    // ========== 箱线图 ==========

    @Tool("生成箱线图，用于展示数据分布和离群值")
    public String createBoxPlot(
            @P("图表标题") String title,
            @P("分组标签(逗号分隔)") String labels,
            @P("各分组的值(分号分隔各组，逗号分隔值)") String groupData) throws Exception {

        var dataset = new DefaultBoxAndWhiskerCategoryDataset();
        String[] labelArray = labels.split(",");
        String[] dataBlocks = groupData.split(";");
        for (int i = 0; i < Math.min(labelArray.length, dataBlocks.length); i++) {
            String[] values = dataBlocks[i].trim().split(",");
            List<Double> doubles = new ArrayList<>();
            for (String v : values) {
                try { doubles.add(Double.parseDouble(v.trim())); } catch (NumberFormatException ignored) {}
            }
            if (doubles.size() >= 5) {
                dataset.add(doubles, labelArray[i].trim(), "1");
            }
        }

        JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(title, "Group", "Value", dataset, false);
        var renderer = (org.jfree.chart.renderer.category.BoxAndWhiskerRenderer) chart.getCategoryPlot().getRenderer();
        renderer.setMeanVisible(false);
        renderer.setMedianVisible(true);
        renderer.setWhiskerWidth(0.5);
        renderer.setMaximumBarWidth(0.15);
        for (int i = 0; i < labelArray.length && i < dataBlocks.length; i++) {
            renderer.setSeriesPaint(i, ChartStyle.color(i));
            renderer.setSeriesOutlinePaint(i, ChartStyle.TEXT_PRIMARY);
            renderer.setSeriesFillPaint(i, ChartStyle.color(i));
        }
        ChartStyle.applyStyle(chart);
        return saveChart(chart, "boxplot");
    }

    // ========== 工具方法 ==========

    private String saveChart(JFreeChart chart, String prefix) throws Exception {
        String filename = String.format("%s_%s.png", prefix, TIMESTAMP.format(LocalDateTime.now()));
        File outputFile = new File(OUTPUT_DIR, filename).getAbsoluteFile();
        ChartUtils.saveChartAsPNG(outputFile, chart, ChartStyle.CHART_WIDTH, ChartStyle.CHART_HEIGHT);
        return outputFile.getAbsolutePath();
    }

}
