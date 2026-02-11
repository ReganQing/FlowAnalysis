# 数据分析多智能体系统

## 项目概述

基于 **LangChain4j** 构建的数据分析多智能体系统，实现完整的 CSV 数据处理工作流：
```
CSV 文件 → 文件解析 → 数据清洗 → 多维度分析 → 图表生成 → 数据报告
```

## 技术栈

| 组件 | 技术 | 版本 | 用途 |
|--------|------|------|------|
| AI 框架 | LangChain4j | 1.4.0 | AI 集成和工具调用 |
| 数据分析 | Tablesaw | 0.44.0 | Java 数据分析库（类似 Pandas） |
| 图表生成 | JFreeChart | 1.5.4 | Java 图表库 |
| CSV 解析 | Apache Commons CSV | 1.11.0 | CSV 文件处理 |
| 报告生成 | Flexmark | 0.64.0 | Markdown 转 HTML |

## 系统架构

### 智能体 (Agents)

1. **CSVParserAgent** - CSV 文件解析智能体
   - 读取 CSV 文件
   - 验证数据格式
   - 生成数据摘要

2. **DataCleanerAgent** - 数据清洗智能体
   - 处理缺失值（均值/中位数填充）
   - 去除重复行
   - 检测异常值（IQR 方法）
   - 生成数据质量报告

3. **DataAnalyzerAgent** - 数据分析智能体
   - 描述性统计（均值、中位数、标准差等）
   - 销售趋势分析
   - 产品销售排行（Top N）
   - 区域销售分析
   - 销售渠道对比
   - 相关性分析
   - 分组聚合分析

4. **ChartGeneratorAgent** - 图表生成智能体
   - 生成统计图表（柱状图、饼图）
   - 支持扩展到更多图表类型

5. **ReportGeneratorAgent** - 报告生成智能体
   - 生成 Markdown 格式报告
   - 包含数据概览、分析结果、图表链接
   - 提供业务建议

### 工作流编排

**当前实现：简化版顺序执行**
- 使用简单的顺序执行模式
- 各智能体依次执行
- 状态通过 Map 在智能体间传递
- 错误处理和日志记录

**未来计划：LangGraph4J 完整集成** ⚠️
- [ ] 实现真正的状态图（StateGraph）
- [ ] 添加条件路由（基于分析结果动态选择下一步）
- [ ] 支持并行执行（独立智能体并发运行）
- [ ] 添加检查点（Checkpointing - 保存和恢复状态）
- [ ] 实现循环（迭代优化直到满足条件）
- [ ] 可视化工作流执行图（Mermaid/PlantUML）

**LangGraph4J 集成说明：**
- 项目最初尝试集成 LangGraph4J 1.8.0
- 发现文档 API 与实际 JAR 不匹配
- 多个核心类无法编译（`BaseLangChain4jState`, `NodeAction`, `EdgeAction` 等）
- **决定采用简化顺序执行，确保系统可用和可维护**

## 项目结构

```
src/main/java/dataAnalysis/
├── AnalysisState.java              # 状态管理（简化版）
├── DataAnalysisWorkflow.java       # 工作流编排器
├── DataAnalysisDemo.java          # 主入口和示例
├── agents/
│   ├── CSVParserAgent.java        # CSV 解析智能体
│   ├── DataCleanerAgent.java     # 数据清洗智能体
│   ├── DataAnalyzerAgent.java     # 数据分析智能体
│   ├── ChartGeneratorAgent.java   # 图表生成智能体
│   └── ReportGeneratorAgent.java # 报告生成智能体
└── tools/
    ├── CSVTools.java              # CSV 处理工具
    ├── DataCleaningTools.java     # 数据清洗工具
    ├── AnalysisTools.java         # 分析工具
    ├── ChartTools.java            # 图表工具
    └── ReportTools.java          # 报告工具
```

## 使用方法

### 编译
```bash
mvn clean compile
```

### 运行
```bash
mvn exec:java -Dexec.mainClass="dataAnalysis.DataAnalysisDemo"
```

### 输出
- **数据报告**：`output/reports/sales_analysis_report_*.md`
- **图表文件**：`output/charts/*.png`
- **日志输出**：控制台输出工作流执行步骤

## 示例数据格式

系统支持标准销售/电商数据格式：

```csv
date,product_id,product_name,category,quantity,unit_price,amount,region,sales_channel
2024-01-01,P001,笔记本电脑,电子产品,2,5999,11998,北京,线上
2024-01-02,P002,无线鼠标,电子产品,5,199,995,上海,线下
```

**支持的字段类型：**
- 日期列：`LOCAL_DATE`
- 字符串列：`STRING`
- 数值列：`INTEGER`, `DOUBLE`, `LONG`
- 分类列：`STRING`

## 已解决的问题

### Tablesaw API 兼容性
- ✅ 修复 `summarize()` 返回 `Summarizer` 而非 `Table`
- ✅ 添加 `.apply()` 调用获取实际表格
- ✅ 修复 `sortDescendingOn()` 接受列名（String）而非索引
- ✅ 修复 `getDouble()` 在 `Column<?>` 上不可用
- ✅ 修复 `stdDev()` → `standardDeviation()`
- ✅ 修复 `missingCount()` → `countMissing()`
- ✅ 修复 `correlation()` 方法签名

### Java 版本兼容性
- ✅ 移除 Java 17 模式匹配（`instanceof NumberColumn<?>`）
- ✅ 使用传统 instanceof 和显式转换
- ✅ 确保与 Java 17 兼容

### 异常处理
- ✅ 所有智能体方法都有 try-catch
- ✅ 错误时返回有意义的错误信息
- ✅ 工作流在某个步骤失败时可继续

## 未来增强方向

### 短期（1-2 周）
- [ ] 添加更多图表类型（折线图、散点图、热力图）
- [ ] 完善相关性分析实现
- [ ] 支持更多数据格式（Excel, JSON）
- [ ] 添加配置文件支持
- [ ] 改进错误处理和恢复机制

### 中期（1-2 月）
- [ ] 集成真正的 LangGraph4J 工作流引擎
- [ ] 添加 AI 智能路由（基于数据特征自动选择分析方法）
- [ ] 实现工作流可视化
- [ ] 添加 Web UI 界面

### 长期（3-6 月）
- [ ] 支持实时数据流处理
- [ ] 分布式执行（多节点并行）
- [ ] 持久化存储和查询历史分析
- [ ] 多租户支持

## 许可证

本项目仅作为学习和演示使用。

## 参考资料

- [LangChain4j 官方文档](https://docs.langchain4j.dev/)
- [Tablesaw 用户指南](https://jtablesaw.github.io/tablesaw/userguide/toc.html)
- [JFreeChart 文档](https://www.jfree.org/jfreechart/)
- [Apache Commons CSV](https://commons.apache.org/proper/commons-csv/)
