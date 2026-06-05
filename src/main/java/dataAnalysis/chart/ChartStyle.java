package dataAnalysis.chart;

import org.jfree.chart.ChartTheme;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;

import java.awt.*;

/**
 * 图表统一样式
 * 设计原则：清晰简洁、高贵优雅、克制用色
 * 禁止蓝绿渐变、红紫渐变
 */
public final class ChartStyle {

    // ---- 配色方案：低饱和、高级灰度 + 暖金点缀 ----
    public static final Color[] PALETTE = {
        new Color(74, 74, 74),     // 深灰 CHART_1
        new Color(200, 169, 126),  // 暖金 CHART_2 (唯一强调色)
        new Color(122, 154, 122),  // 灰绿 CHART_3
        new Color(138, 122, 106),  // 暖褐 CHART_4
        new Color(106, 122, 138),  // 灰蓝 CHART_5
        new Color(154, 138, 122),  // 浅褐 CHART_6
        new Color(90, 90, 90),     // 深灰2 CHART_7
        new Color(170, 155, 140),  // 浅金 CHART_8
    };

    // ---- 洞察等级色 ----
    public static final Color CRITICAL_COLOR = new Color(184, 84, 80);    // 深砖红
    public static final Color WARNING_COLOR  = new Color(200, 155, 60);    // 暗金
    public static final Color INFO_COLOR     = new Color(90, 122, 154);    // 灰蓝
    public static final Color POSITIVE_COLOR = new Color(90, 138, 106);   // 灰绿

    // ---- 基础色 ----
    public static final Color BG_WHITE        = new Color(255, 255, 255);
    public static final Color TRANSPARENT     = new Color(0, 0, 0, 0);
    public static final Color TEXT_PRIMARY    = new Color(26, 26, 26);
    public static final Color TEXT_SECONDARY  = new Color(107, 107, 107);
    public static final Color DIVIDER         = new Color(224, 224, 224);
    public static final Color ACCENT          = new Color(200, 169, 126);    // 暖金

    // ---- 字体 ----
    public static final Font TITLE_FONT  = new Font("Microsoft YaHei", Font.BOLD, 16);
    public static final Font LABEL_FONT  = new Font("Microsoft YaHei", Font.PLAIN, 12);
    public static final Font TICK_FONT   = new Font("Microsoft YaHei", Font.PLAIN, 11);
    public static final Font NUMBER_FONT = new Font("Georgia", Font.PLAIN, 11);
    public static final Font LEGEND_FONT = new Font("Microsoft YaHei", Font.PLAIN, 11);

    // ---- 尺寸 ----
    public static final int CHART_WIDTH = 900;
    public static final int CHART_HEIGHT = 500;

    private ChartStyle() {}

    /**
     * 获取指定索引的颜色
     */
    public static Color color(int index) {
        return PALETTE[index % PALETTE.length];
    }

    /**
     * 应用统一风格到JFreeChart
     */
    public static void applyStyle(JFreeChart chart) {
        // 背景
        chart.setBackgroundPaint(BG_WHITE);
        chart.setBorderVisible(false);

        // 标题
        if (chart.getTitle() != null) {
            chart.getTitle().setFont(TITLE_FONT);
            chart.getTitle().setPaint(TEXT_PRIMARY);
        }

        // 图例
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(BG_WHITE);
            chart.getLegend().setItemFont(LEGEND_FONT);
        }

        // 根据图表类型定制各组件
        if (chart.getPlot() instanceof CategoryPlot plot) {
            applyCategoryPlotStyle(plot);
        } else if (chart.getPlot() instanceof XYPlot plot) {
            applyXYPlotStyle(plot);
        } else if (chart.getPlot() instanceof PiePlot plot) {
            applyPiePlotStyle(plot);
        }
    }

    private static void applyCategoryPlotStyle(CategoryPlot plot) {
        plot.setBackgroundPaint(BG_WHITE);
        plot.setDomainGridlinePaint(TRANSPARENT);
        plot.setRangeGridlinePaint(DIVIDER);
        plot.setRangeGridlineStroke(new BasicStroke(0.5f));
        plot.setOutlinePaint(TRANSPARENT);
        plot.setRangeAxisLocation(org.jfree.chart.axis.AxisLocation.BOTTOM_OR_LEFT);

        // X轴
        CategoryAxis xAxis = (CategoryAxis) plot.getDomainAxis();
        xAxis.setTickLabelFont(LABEL_FONT);
        xAxis.setTickLabelPaint(TEXT_SECONDARY);
        xAxis.setAxisLinePaint(DIVIDER);
        xAxis.setTickMarkPaint(DIVIDER);

        // Y轴
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setTickLabelFont(NUMBER_FONT);
        yAxis.setTickLabelPaint(TEXT_SECONDARY);
        yAxis.setAxisLinePaint(DIVIDER);
        yAxis.setTickMarkPaint(DIVIDER);
        yAxis.setAutoRangeIncludesZero(true);

        // 渲染器着色
        var renderer = plot.getRenderer();
        if (renderer instanceof BarRenderer barRenderer) {
            barRenderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
            barRenderer.setShadowVisible(false);
            barRenderer.setMaximumBarWidth(0.08);
            for (int i = 0; i < plot.getDataset().getRowCount(); i++) {
                barRenderer.setSeriesPaint(i, color(i));
            }
        }
    }

    private static void applyXYPlotStyle(XYPlot plot) {
        plot.setBackgroundPaint(BG_WHITE);
        plot.setDomainGridlinePaint(TRANSPARENT);
        plot.setRangeGridlinePaint(DIVIDER);
        plot.setRangeGridlineStroke(new BasicStroke(0.5f));
        plot.setOutlinePaint(TRANSPARENT);

        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setTickLabelFont(NUMBER_FONT);
        xAxis.setTickLabelPaint(TEXT_SECONDARY);
        xAxis.setAxisLinePaint(DIVIDER);
        xAxis.setTickMarkPaint(DIVIDER);

        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setTickLabelFont(NUMBER_FONT);
        yAxis.setTickLabelPaint(TEXT_SECONDARY);
        yAxis.setAxisLinePaint(DIVIDER);
        yAxis.setTickMarkPaint(DIVIDER);
        yAxis.setAutoRangeIncludesZero(true);
    }

    private static void applyPiePlotStyle(PiePlot plot) {
        plot.setBackgroundPaint(BG_WHITE);
        plot.setOutlinePaint(TRANSPARENT);
        plot.setLabelFont(LABEL_FONT);
        plot.setLabelPaint(TEXT_PRIMARY);
        plot.setLabelGap(0.02);
        plot.setInteriorGap(0);

        for (int i = 0; i < plot.getDataset().getItemCount(); i++) {
            plot.setSectionPaint(i, color(i));
            plot.setSectionOutlinePaint(i, BG_WHITE);
        }
    }
}
