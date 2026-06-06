package dataAnalysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Random;

/**
 * 数据分析多智能体应用 - 主入口
 *
 * 演示如何使用 LangGraph4J 和 LangChain4j 构建数据分析多智能体系统
 *
 * 流程: CSV文件解析 -> 数据清洗 -> 多维度分析 -> 数据图表生成 -> 生成数据分析报告
 */
public class DataAnalysisDemo {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("数据分析多智能体系统");
        System.out.println("基于 LangGraph4J + LangChain4j");
        System.out.println("========================================\n");

        // 检查命令行参数
        String csvPath;
        if (args.length > 0) {
            csvPath = args[0];
        } else {
            // 生成示例数据
            csvPath = generateSampleData();
            System.out.println("已生成示例数据: " + csvPath);
            System.out.println();
        }

        // 创建工作流
        DataAnalysisGraph graph = new DataAnalysisGraph();

        // 执行分析
        try {
            DataAnalysisGraph.AnalysisResult result = graph.execute(csvPath);

            // 输出结果
            System.out.println(result);
            System.out.println("\n分析完成！");
            System.out.println("请查看报告: " + result.getReportPath());
            if (!result.getErrors().isEmpty()) {
                System.out.println("警告: " + result.getErrors());
            }

        } catch (Exception e) {
            System.err.println("分析失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("数据分析管线执行失败", e);
        }
    }

    /**
     * 生成示例销售数据 CSV 文件
     */
    private static String generateSampleData() {
        String outputDir = "sample_data/";
        new File(outputDir).mkdirs();

        String filename = outputDir + "sales_data_sample.csv";
        Random random = new Random();

        try (FileWriter writer = new FileWriter(filename)) {
            // 写入表头
            writer.write("date,product_id,product_name,category,quantity,unit_price,amount,region,sales_channel\n");

            // 生成过去 90 天的销售数据
            LocalDate startDate = LocalDate.now().minusDays(90);

            String[] products = {
                    "P001,笔记本电脑,电子产品",
                    "P002,无线鼠标,电子产品",
                    "P003,机械键盘,电子产品",
                    "P004,显示器,电子产品",
                    "P005,耳机,电子产品",
                    "P006,充电宝,配件",
                    "P007,数据线,配件",
                    "P008,电脑包,配件",
                    "P009,屏幕清洁剂,配件",
                    "P010,USB转接器,配件"
            };

            String[] regions = {"北京", "上海", "广州", "深圳", "杭州", "成都", "武汉", "西安"};
            String[] channels = {"线上", "线下"};

            // 生成 500 条销售记录
            for (int i = 0; i < 500; i++) {
                LocalDate date = startDate.plusDays(random.nextInt(90));
                String product = products[random.nextInt(products.length)];
                String[] productParts = product.split(",");

                int quantity = random.nextInt(10) + 1;
                double unitPrice = switch (productParts[0]) {
                    case "P001" -> 5999.0;
                    case "P002" -> 199.0;
                    case "P003" -> 499.0;
                    case "P004" -> 1999.0;
                    case "P005" -> 299.0;
                    case "P006" -> 99.0;
                    case "P007" -> 29.0;
                    case "P008" -> 159.0;
                    case "P009" -> 39.0;
                    case "P010" -> 79.0;
                    default -> 100.0;
                };

                double amount = quantity * unitPrice;
                String region = regions[random.nextInt(regions.length)];
                String channel = channels[random.nextInt(channels.length)];

                writer.write(String.format("%s,%s,%s,%s,%d,%.2f,%.2f,%s,%s\n",
                        date, productParts[0], productParts[1], productParts[2],
                        quantity, unitPrice, amount, region, channel));
            }

            System.out.println("已生成 500 条销售记录");

        } catch (IOException e) {
            throw new RuntimeException("生成示例数据失败", e);
        }

        return filename;
    }
}
