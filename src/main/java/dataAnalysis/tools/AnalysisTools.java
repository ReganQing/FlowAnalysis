package dataAnalysis.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import tech.tablesaw.api.Table;
import tech.tablesaw.api.NumberColumn;
import tech.tablesaw.aggregate.AggregateFunctions;
import tech.tablesaw.columns.Column;

import java.time.LocalDate;
import java.util.*;

/**
 * 数据分析工具
 * 提供销售数据的各种分析功能，包括趋势分析、排行分析、区域分析等
 */
public class AnalysisTools {

    /**
     * 生成描述性统计
     */
    @Tool("对数值列生成描述性统计，包括均值、中位数、标准差、最小值、最大值等")
    public String descriptiveStats(@P("数据表") Table data) {
        StringBuilder stats = new StringBuilder();
        stats.append("{");

        List<String> statEntries = new ArrayList<>();
        for (Column<?> col : data.columns()) {
            if (col instanceof NumberColumn) {
                NumberColumn numCol = (NumberColumn) col;
                if (!numCol.isEmpty()) {
                    StringBuilder colStats = new StringBuilder();
                    colStats.append("\"").append(col.name()).append("\": {");
                    colStats.append("\"mean\": ").append(numCol.mean()).append(", ");
                    colStats.append("\"median\": ").append(numCol.median()).append(", ");
                    colStats.append("\"std_dev\": ").append(numCol.standardDeviation()).append(", ");
                    colStats.append("\"min\": ").append(numCol.min()).append(", ");
                    colStats.append("\"max\": ").append(numCol.max()).append(", ");
                    colStats.append("\"sum\": ").append(numCol.sum());
                    colStats.append("}");
                    statEntries.add(colStats.toString());
                }
            }
        }

        stats.append("\"statistics\": {");
        stats.append(String.join(", ", statEntries));
        stats.append("}}");

        return stats.toString();
    }

    /**
     * 销售趋势分析
     */
    @Tool("按时间维度分析销售趋势，支持按日、周、月聚合")
    public String salesTrendAnalysis(
            @P("数据表") Table data,
            @P("日期列名") String dateColumn,
            @P("销售额列名") String amountColumn,
            @P("时间粒度: day/week/month") String granularity) {

        if (!data.containsColumn(dateColumn) || !data.containsColumn(amountColumn)) {
            return "{\"error\": \"指定的列不存在\"}";
        }

        try {
            Table result = data.summarize(amountColumn, AggregateFunctions.sum)
                    .by(dateColumn);
            result = result.sortOn(result.column(0).name());

            StringBuilder output = new StringBuilder();
            output.append("{\"trend\": [");

            for (int i = 0; i < result.rowCount(); i++) {
                if (i > 0) output.append(", ");
                String date = result.column(0).getString(i);
                double amount = ((NumberColumn) result.column(1)).getDouble(i);
                output.append(String.format("{\"date\": \"%s\", \"amount\": %.2f}", date, amount));
            }

            output.append("]}");
            return output.toString();
        } catch (Exception e) {
            return String.format("{\"error\": \"趋势分析失败: %s\"}", e.getMessage());
        }
    }

    /**
     * 产品销售排行
     */
    @Tool("计算产品销售排行榜，返回 Top N 产品")
    public String topSellingProducts(
            @P("数据表") Table data,
            @P("产品ID列名") String productIdColumn,
            @P("产品名列名") String productNameColumn,
            @P("销售额列名") String amountColumn,
            @P("Top N 数量") int n) {

        if (!data.containsColumn(productNameColumn) || !data.containsColumn(amountColumn)) {
            return "{\"error\": \"指定的列不存在\"}";
        }

        try {
            Table result = data.summarize(amountColumn, AggregateFunctions.sum)
                    .by(productNameColumn);
            String sumCol = result.column(1).name();
            result = result.sortDescendingOn(sumCol);

            StringBuilder output = new StringBuilder();
            output.append("{\"top_products\": [");

            int count = Math.min(n, result.rowCount());
            for (int i = 0; i < count; i++) {
                if (i > 0) output.append(", ");
                String product = result.column(0).getString(i);
                double amount = ((NumberColumn) result.column(1)).getDouble(i);
                output.append(String.format("{\"rank\": %d, \"product\": \"%s\", \"amount\": %.2f}",
                        i + 1, product, amount));
            }

            output.append("]}");
            return output.toString();
        } catch (Exception e) {
            return String.format("{\"error\": \"%s\"}", e.getMessage());
        }
    }

    /**
     * 区域销售分析
     */
    @Tool("按地区统计销售额和订单量")
    public String regionalSalesAnalysis(
            @P("数据表") Table data,
            @P("地区列名") String regionColumn,
            @P("销售额列名") String amountColumn) {

        if (!data.containsColumn(regionColumn) || !data.containsColumn(amountColumn)) {
            return "{\"error\": \"指定的列不存在\"}";
        }

        try {
            Table amountSum = data.summarize(amountColumn, AggregateFunctions.sum)
                    .by(regionColumn);
            Table orderCount = data.summarize(amountColumn, AggregateFunctions.count)
                    .by(regionColumn);

            StringBuilder output = new StringBuilder();
            output.append("{\"regions\": [");

            for (int i = 0; i < amountSum.rowCount(); i++) {
                if (i > 0) output.append(", ");
                String region = amountSum.column(0).getString(i);
                double amount = ((NumberColumn) amountSum.column(1)).getDouble(i);
                double count = ((NumberColumn) orderCount.column(1)).getDouble(i);
                output.append(String.format("{\"region\": \"%s\", \"amount\": %.2f, \"orders\": %d}",
                        region, amount, (int) count));
            }

            output.append("]}");
            return output.toString();
        } catch (Exception e) {
            return String.format("{\"error\": \"%s\"}", e.getMessage());
        }
    }

    /**
     * 渠道销售对比
     */
    @Tool("对比不同销售渠道（线上/线下）的销售表现")
    public String channelComparison(
            @P("数据表") Table data,
            @P("渠道列名") String channelColumn,
            @P("销售额列名") String amountColumn) {

        if (!data.containsColumn(channelColumn) || !data.containsColumn(amountColumn)) {
            return "{\"error\": \"指定的列不存在\"}";
        }

        try {
            Table result = data.summarize(amountColumn, AggregateFunctions.sum)
                    .by(channelColumn);

            double totalAmount = 0;
            for (int i = 0; i < result.rowCount(); i++) {
                totalAmount += ((NumberColumn) result.column(1)).getDouble(i);
            }

            StringBuilder output = new StringBuilder();
            output.append("{\"channels\": [");

            for (int i = 0; i < result.rowCount(); i++) {
                if (i > 0) output.append(", ");
                String channel = result.column(0).getString(i);
                double amount = ((NumberColumn) result.column(1)).getDouble(i);
                double percentage = (amount / totalAmount) * 100;
                output.append(String.format("{\"channel\": \"%s\", \"amount\": %.2f, \"percentage\": %.2f}",
                        channel, amount, percentage));
            }

            output.append("], \"total_amount\": ").append(totalAmount).append("}");
            return output.toString();
        } catch (Exception e) {
            return String.format("{\"error\": \"%s\"}", e.getMessage());
        }
    }

    /**
     * 相关性分析
     */
    @Tool("计算两个数值列之间的相关性系数（皮尔逊相关系数）")
    public String correlationAnalysis(
            @P("数据表") Table data,
            @P("第一个列名") String column1,
            @P("第二个列名") String column2) {

        if (!data.containsColumn(column1) || !data.containsColumn(column2)) {
            return "{\"error\": \"指定的列不存在\"}";
        }

        Column<?> col1 = data.column(column1);
        Column<?> col2 = data.column(column2);

        if (!(col1 instanceof NumberColumn) || !(col2 instanceof NumberColumn)) {
            return "{\"error\": \"两个列都必须是数值类型\"}";
        }

        NumberColumn numCol1 = (NumberColumn) col1;
        NumberColumn numCol2 = (NumberColumn) col2;

        int n = Math.min(numCol1.size(), numCol2.size());
        double mean1 = numCol1.mean();
        double mean2 = numCol2.mean();

        double sumXY = 0, sumX2 = 0, sumY2 = 0;
        int validPairs = 0;
        for (int i = 0; i < n; i++) {
            if (!numCol1.isMissing(i) && !numCol2.isMissing(i)) {
                double dx = numCol1.getDouble(i) - mean1;
                double dy = numCol2.getDouble(i) - mean2;
                sumXY += dx * dy;
                sumX2 += dx * dx;
                sumY2 += dy * dy;
                validPairs++;
            }
        }

        double correlation = 0.0;
        if (sumX2 > 0 && sumY2 > 0) {
            correlation = sumXY / Math.sqrt(sumX2 * sumY2);
        }

        String strength = interpretCorrelation(correlation);

        return String.format(
            "{\"column1\": \"%s\", \"column2\": \"%s\", \"correlation\": %.4f, \"strength\": \"%s\", \"valid_pairs\": %d}",
            column1, column2, correlation, strength, validPairs);
    }

    private String interpretCorrelation(double corr) {
        double abs = Math.abs(corr);
        if (abs < 0.3) return "弱相关";
        if (abs < 0.7) return "中等相关";
        return "强相关";
    }

    /**
     * 分类汇总分析
     */
    @Tool("按指定分类列进行汇总统计")
    public String groupByAnalysis(
            @P("数据表") Table data,
            @P("分组列名") String groupColumn,
            @P("数值列名") String valueColumn,
            @P("聚合函数: sum/mean/count/max/min") String aggFunction) {

        if (!data.containsColumn(groupColumn) || !data.containsColumn(valueColumn)) {
            return "{\"error\": \"指定的列不存在\"}";
        }

        try {
            Table result = data.summarize(valueColumn,
                switch (aggFunction.toLowerCase()) {
                    case "mean" -> AggregateFunctions.mean;
                    case "count" -> AggregateFunctions.count;
                    case "max" -> AggregateFunctions.max;
                    case "min" -> AggregateFunctions.min;
                    default -> AggregateFunctions.sum;
                }
            ).by(groupColumn);

            StringBuilder output = new StringBuilder();
            output.append("{\"groups\": [");

            for (int i = 0; i < result.rowCount(); i++) {
                if (i > 0) output.append(", ");
                String group = result.column(0).getString(i);
                String value = result.column(1).getString(i);
                output.append(String.format("{\"group\": \"%s\", \"value\": %s}", group, value));
            }

            output.append("]}");
            return output.toString();
        } catch (Exception e) {
            return String.format("{\"error\": \"%s\"}", e.getMessage());
        }
    }
}
