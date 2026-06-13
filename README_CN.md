<div align="center">

# 🤖 LangChain4j 学习项目

**基于 LangChain4j 的 Java AI 应用开发学习与演示平台**

中文 · **[English](README.md)**

[![Java](https://img.shields.io/badge/Java-17+-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![LangChain4j](https://img.shields.io/badge/LangChain4j-1.4.0-6366F1)](https://github.com/langchain4j/langchain4j)
[![LangGraph4J](https://img.shields.io/badge/LangGraph4J-1.8.17-10B981)](https://github.com/bsorrentino/langgraph4j)
[![Maven](https://img.shields.io/badge/Maven-3.x-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

</div>

---

## 🌟 项目简介

本项目是一个基于 **LangChain4j** 构建的综合性 Java 学习与演示平台，全面探索 AI 应用开发的各个方面：聊天对话、对话记忆、工具/函数调用、流式响应、结构化输出、图像生成，以及基于 **LangGraph4J** 的复杂多智能体数据分析流水线。

同时包含一个功能完整的 **JavaFX 桌面端应用**，具有精心设计的深色主题 UI、SQLite 数据持久化和流式 Markdown 渲染。

> **LLM 提供商：** [阿里云百炼 DashScope](https://dashscope.console.aliyun.com/)（OpenAI 兼容 API）— 需要设置 `DASHSCOPE_API_KEY` 环境变量。

---

## ✨ 功能特性

### 🧠 AI 能力（学习演示）

| 功能 | 说明 |
|------|------|
| **AI 服务** | 通过注解 Java 接口，由 LangChain4j 自动生成实现 |
| **对话记忆** | 滑动窗口记忆与按用户隔离记忆 |
| **工具调用** | `@Tool` 注解方法，框架自动发现与调用 |
| **流式响应** | 基于 `TokenStream` API 的实时逐 Token 输出 |
| **结构化输出** | JSON Schema 手动定义和 POJO 自动推断两种方式 |
| **图像生成** | 基于 DashScope SDK 的文生图 |
| **情感分析** | AI 驱动的文本分类 |

### 🤖 多智能体数据分析流水线

7 节点 **LangGraph4J** 状态图，将原始 CSV 数据转化为专业分析报告：

```
CSV解析 → 数据清洗 → AI规划 → [数据分析 | 洞察生成 | 图表生成] → 报告生成
```

- **AI 驱动规划** — 由大语言模型决定执行哪些分析
- **智能数据处理** — 概要 → 采样 → 聚合三层策略，带 Token 预算管理
- **7 种图表类型** — 柱状图、饼图、折线图、散点图、组合图、箱线图、统计图
- **专业报告** — 单文件 HTML，内嵌 CSS + Base64 图表
- **智能模型路由** — 基于任务类型选择最优模型，优化成本

### 💻 桌面端应用（JavaFX）

现代化 AI 聊天助手桌面应用：

- 🎨 **深色主题 UI** — 午夜蓝 (#0A0E27) 搭配金色点缀
- 💬 **流式聊天** — 实时逐 Token 响应渲染
- 📝 **Markdown 渲染** — 双渲染器：轻量 TextFlow 用于流式展示，WebView 用于最终渲染（支持表格）
- 📎 **文件上传** — 拖放上传 .csv/.xls/.xlsx 文件（最大 50MB），自动 Excel→CSV 转换
- 📊 **文件预览** — 右侧面板提供 CSV 数据表、HTML 报告、图片预览
- 🔧 **工具调用可视化** — 交互式卡片展示 AI 工具执行过程
- 📈 **管线进度** — 7 阶段进度条，含逐节点日志，用于数据分析任务
- 💾 **会话管理** — 自动保存、恢复上次会话、AI 生成标题
- ⚙️ **设置面板** — 模型配置、API Key 管理
- 🗃️ **SQLite 存储** — 嵌入式数据库，自动初始化表结构

<!-- 取消注释以添加截图：
### 📸 应用截图

| 聊天界面 | 数据分析报告 |
|:-:|:-:|
| ![聊天界面](docs/screenshots/chat.png) | ![报告](docs/screenshots/report.png) |
-->

---

## 🏗️ 架构设计

```
┌─────────────────────────────────────────────────────┐
│                    JavaFX 21 界面                     │
│          (FXML + CSS 深色主题 + WebView)              │
├─────────────────────────────────────────────────────┤
│              服务层 (依赖注入)                         │
│   ChatService · SessionService · TitleService        │
│   AnalysisService · FileUploadService · ModelService │
├─────────────────────────────────────────────────────┤
│           仓储层 (DAO 模式)                            │
│          SQLite · DatabaseManager 单例                │
├─────────────────────────────────────────────────────┤
│              LangChain4j 1.4.0 核心                   │
│    AiServices · ChatMemory · @Tool · TokenStream     │
├─────────────────────────────────────────────────────┤
│           LangGraph4J 1.8.17 (流水线)                 │
│    StateGraph · AgentState · 条件路由                  │
├─────────────────────────────────────────────────────┤
│    DashScope API · Ollama · Tablesaw · JFreeChart    │
└─────────────────────────────────────────────────────┘
```

### 项目结构

```
src/
├── main/java/
│   ├── model/                    # 共享基础设施：ChatModelCreator、模型配置
│   ├── dataAnalysis/             # 多智能体数据分析流水线
│   │   ├── chart/                # 图表生成 (JFreeChart)
│   │   ├── model/                # 领域模型 (AnalysisPlan, Insight 等)
│   │   ├── nodes/                # 7 个 LangGraph4J 流水线节点
│   │   ├── processing/           # 数据概要与 Token 预算管理
│   │   ├── report/               # HTML 报告生成器
│   │   ├── router/               # 模型路由（接口 + 实现）
│   │   └── tools/                # 各节点的 @Tool 注解方法
│   └── desktop/                  # JavaFX 桌面端应用
│       ├── app/                  # 应用入口
│       ├── model/                # 领域模型（不可变 Record）
│       ├── repository/           # 数据访问层 (SQLite)
│       ├── service/              # 聊天、会话、分析、文件上传
│       └── view/                 # 控制器与自定义 UI 组件
│           └── component/        # MessageBubble、MarkdownTextFlow、FilePreviewPanel 等
├── main/resources/
│   └── desktop/                  # FXML 布局、CSS 主题、数据库 Schema
└── test/java/                    # 20+ 个学习演示文件
    ├── AIServicesDemo            # AI 服务基础
    ├── ChatMemoryDemo            # 对话记忆
    ├── StreamingChatDemo         # 流式响应
    ├── Text2Image                # 图像生成
    ├── Tool/                     # 工具调用演示
    ├── StructuralOutput/         # 结构化输出演示
    └── dataAnalysis/             # 流水线入口
```

---

## 🚀 快速开始

### 环境要求

- **Java 17+**（推荐 OpenJDK）
- **Maven 3.x**
- **DashScope API Key** — [点击获取](https://dashscope.console.aliyun.com/)

### 安装与配置

```bash
# 1. 克隆仓库
git clone https://github.com/qinrongxin/LangChain4j.git
cd LangChain4j

# 2. 设置 API Key
# Linux/macOS
export DASHSCOPE_API_KEY=你的API密钥

# Windows (PowerShell)
$env:DASHSCOPE_API_KEY="你的API密钥"

# Windows (CMD)
set DASHSCOPE_API_KEY=你的API密钥
```

### 运行演示

```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test

# 运行学习演示
mvn exec:java -Dexec.mainClass="AIServicesDemo"
mvn exec:java -Dexec.mainClass="ChatMemoryDemo"
mvn exec:java -Dexec.mainClass="StreamingChatDemo"
mvn exec:java -Dexec.mainClass="Text2Image"
mvn exec:java -Dexec.mainClass="SentimentClassification"

# 运行多智能体数据分析流水线
mvn exec:java -Dexec.mainClass="dataAnalysis.DataAnalysisDemo"
```

### 运行桌面应用

> 请使用 `mvn javafx:run` 启动（而非 `mvn exec:java`），以确保 JavaFX 的 `jfxwebkit` 原生库正常加载，WebView 才能内嵌渲染报告。

```bash
mvn clean compile
mvn javafx:run
```

> 首次启动时应用会自动在 `data/assistant.db` 初始化 SQLite 数据库。

---

## 📦 依赖清单

| 依赖 | 版本 | 用途 |
|---|---|---|
| [LangChain4j](https://github.com/langchain4j/langchain4j) | 1.4.0 | 核心 AI 框架 |
| [LangGraph4J](https://github.com/bsorrentino/langgraph4j) | 1.8.17 | 状态图工作流引擎 |
| JavaFX | 21 | 桌面端 UI 框架 |
| SQLite JDBC | 3.45.1.0 | 嵌入式数据库 |
| [Tablesaw](https://github.com/jtablesaw/tablesaw) | 0.44.0 | 数据分析（类 Pandas） |
| [JFreeChart](https://github.com/jfree/jfreechart) | 1.5.4 | 图表生成 |
| Apache Commons CSV | 1.11.0 | CSV 解析 |
| [Apache POI](https://poi.apache.org/) | 5.2.5 | Excel .xls/.xlsx 读取与转换 |
| DashScope SDK | 2.21.3 | DashScope API 直接访问 |
| CommonMark | 0.22.0 | Markdown 解析（流式渲染器） |
| Flexmark | 0.64.0 | Markdown 处理（报告生成） |
| Jackson | 2.17.0 | JSON 序列化 |
| Lombok | 1.18.38 | 简化样板代码 |
| JUnit 5 | 5.11.4 | 单元测试 |

---

## 🎯 核心模式

### AI 服务 — 注解接口

LangChain4j 中创建 AI 服务最简单的方式：

```java
public interface AIAssistant {
    @SystemMessage("你是一个有帮助的助手。")
    String chat(@UserMessage String message);
}

AIAssistant assistant = AiServices.create(AIAssistant.class, model);
String response = assistant.chat("你好！");
```

### 工具调用

使用 `@Tool` 注解方法，使其可被 AI 调用：

```java
public class Calculator {
    @Tool("计算一个数的平方根")
    double squareRoot(@P("要计算的数字") double x) {
        return Math.sqrt(x);
    }
}
```

### 多智能体流水线

使用 LangGraph4J 定义有状态工作流：

```java
StateGraph<AgentState> graph = new StateGraph<>(AnalysisState.class, channels)
    .addNode("parser",    new CSVParserNode())
    .addNode("cleaner",   new DataCleanerNode())
    .addNode("planner",   new AIPlannerNode())
    .addNode("analyzer",  new DataAnalyzerNode())
    .addNode("insight",   new InsightNode())
    .addNode("chart",     new ChartGeneratorNode())
    .addNode("report",    new ReportGeneratorNode())
    .addEdge(START, "parser")
    .addEdge("parser", "cleaner")
    .addEdge("cleaner", "planner")
    // ... 基于 AI 规划的条件路由
    .addEdge("report", END);
```

---

## 📖 文档

完整的**[Wiki](docs/wiki/README.md)**文档：

| 文档 | 描述 |
|------|------|
| [架构设计](docs/wiki/architecture.md) | 系统架构、技术栈、设计模式 |
| [数据分析流水线](docs/wiki/data-analysis-pipeline.md) | 基于 LangGraph4J 的多智能体 CSV 分析 |
| [桌面端应用](docs/wiki/desktop-app.md) | JavaFX 聊天应用文档 |
| [API 参考](docs/wiki/api-reference.md) | 核心 API 和扩展点 |
| [开发指南](docs/wiki/development-guide.md) | 设置、构建、运行、贡献指南 |

另见 [CLAUDE.md](CLAUDE.md) 了解 AI 辅助开发指南。

---

## 📄 许可证

本项目基于 MIT 许可证开源 — 详见 [LICENSE](LICENSE) 文件。

---

<div align="center">

**使用 [LangChain4j](https://github.com/langchain4j/langchain4j) 和 [LangGraph4J](https://github.com/bsorrentino/langgraph4j) 用 ❤️ 构建**

</div>
