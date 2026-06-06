<div align="center">

# 🤖 LangChain4j Playground

**A Java learning & demo project showcasing AI capabilities with LangChain4j**

**[中文](README_CN.md)** · English

[![Java](https://img.shields.io/badge/Java-17+-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![LangChain4j](https://img.shields.io/badge/LangChain4j-1.4.0-6366F1)](https://github.com/langchain4j/langchain4j)
[![LangGraph4J](https://img.shields.io/badge/LangGraph4J-1.8.17-10B981)](https://github.com/bsorrentino/langgraph4j)
[![Maven](https://img.shields.io/badge/Maven-3.x-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

</div>

---

## 🌟 Overview

This project is a comprehensive Java learning and demonstration platform built on **LangChain4j** — exploring the full spectrum of AI application development including chat interfaces, conversational memory, tool/function calling, streaming responses, structured output, image generation, and a sophisticated multi-agent data analysis pipeline powered by **LangGraph4J**.

It also features a full-featured **JavaFX desktop application** with a polished dark-theme UI, SQLite persistence, and streaming markdown rendering.

> **LLM Provider:** [Alibaba DashScope](https://dashscope.aliyuncs.com/) (OpenAI-compatible API) — requires `DASHSCOPE_API_KEY` environment variable.

---

## ✨ Features

### 🧠 AI Capabilities (Learning Demos)

| Feature | Description |
|---------|-------------|
| **AI Services** | Annotated Java interfaces auto-implemented by LangChain4j |
| **Chat Memory** | Windowed and per-user conversational memory |
| **Tool Calling** | `@Tool`-annotated methods auto-discovered by the framework |
| **Streaming** | Real-time token streaming with `TokenStream` API |
| **Structured Output** | JSON schema-based and POJO-inferred structured responses |
| **Image Generation** | Text-to-image via DashScope SDK |
| **Sentiment Analysis** | AI-powered text classification |

### 🤖 Multi-Agent Data Analysis Pipeline

A 7-node **LangGraph4J** state graph that transforms raw CSV data into professional analysis reports:

```
CSVParser → DataCleaner → AIPlanner → [DataAnalyzer | Insight | Chart] → ReportGenerator
```

- **AI-Driven Planning** — LLM decides which analyses to perform
- **Smart Data Processing** — Profile → Sample → Aggregate strategy with token budget management
- **7 Chart Types** — Bar, Pie, Line, Scatter, Combo, Boxplot, Statistics
- **Professional Reports** — Single-file HTML with embedded CSS + Base64 charts
- **Intelligent Model Routing** — Task-based model selection for cost optimization

### 💻 Desktop Application (JavaFX)

A modern AI chat assistant desktop app with:

- 🎨 **Dark Theme UI** — Midnight Blue (#0A0E27) with gold accents
- 💬 **Streaming Chat** — Real-time token-by-token response rendering
- 📝 **Markdown Rendering** — Two renderers: lightweight TextFlow for streaming, WebView for final display with table support
- 📎 **File Upload** — Drag-and-drop .csv/.xls/.xlsx files (max 50MB), auto Excel→CSV conversion
- 📊 **File Preview** — Right-side panel with CSV data table, HTML report, and image previews
- 🔧 **Tool Call Visualization** — Interactive cards showing AI tool execution
- 📈 **Pipeline Progress** — 7-stage progress bar with per-node logs for data analysis jobs
- 💾 **Session Management** — Auto-save, restore last session, AI-generated titles
- ⚙️ **Settings Panel** — Model configuration, API key management
- 🗃️ **SQLite Storage** — Embedded database with automatic schema initialization

<!-- Uncomment when screenshots are available:
### 📸 Screenshots

| Chat Interface | Data Analysis Report |
|:-:|:-:|
| ![Chat](docs/screenshots/chat.png) | ![Report](docs/screenshots/report.png) |
-->

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────┐
│                    JavaFX 21 UI                      │
│          (FXML + CSS Dark Theme + WebView)           │
├─────────────────────────────────────────────────────┤
│              Service Layer (DI)                       │
│   ChatService · SessionService · TitleService        │
│   AnalysisService · FileUploadService · ModelService │
├─────────────────────────────────────────────────────┤
│           Repository Layer (DAO Pattern)              │
│          SQLite · DatabaseManager Singleton           │
├─────────────────────────────────────────────────────┤
│              LangChain4j 1.4.0 Core                  │
│    AiServices · ChatMemory · @Tool · TokenStream     │
├─────────────────────────────────────────────────────┤
│           LangGraph4J 1.8.17 (Pipeline)              │
│    StateGraph · AgentState · Conditional Routing     │
├─────────────────────────────────────────────────────┤
│    DashScope API · Ollama · Tablesaw · JFreeChart    │
└─────────────────────────────────────────────────────┘
```

### Project Structure

```
src/
├── main/java/
│   ├── model/                    # Shared: ChatModelCreator, model configs
│   ├── dataAnalysis/             # Multi-agent data analysis pipeline
│   │   ├── chart/                # Chart generation (JFreeChart)
│   │   ├── model/                # Domain models (AnalysisPlan, Insight, etc.)
│   │   ├── nodes/                # 7 LangGraph4J pipeline nodes
│   │   ├── processing/           # Data profiling & token budget management
│   │   ├── report/               # HTML report generator
│   │   ├── router/               # Model routing (interface + implementations)
│   │   └── tools/                # @Tool-annotated methods for each node
│   └── desktop/                  # JavaFX desktop application
│       ├── app/                  # Application entry point
│       ├── model/                # Domain models (immutable records)
│       ├── repository/           # Data access layer (SQLite)
│       ├── service/              # Chat, Session, Analysis, FileUpload
│       └── view/                 # Controllers & custom UI components
│           └── component/        # MessageBubble, MarkdownTextFlow, FilePreviewPanel, etc.
├── main/resources/
│   └── desktop/                  # FXML layouts, CSS theme, DB schema
└── test/java/                    # 20+ learning demo files
    ├── AIServicesDemo            # AI Services basics
    ├── ChatMemoryDemo            # Conversational memory
    ├── StreamingChatDemo         # Streaming responses
    ├── Text2Image                # Image generation
    ├── Tool/                     # Tool calling demos
    ├── StructuralOutput/         # Structured output demos
    └── dataAnalysis/             # Pipeline entry point
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 17+** (OpenJDK recommended)
- **Maven 3.x**
- **DashScope API Key** — [Get one here](https://dashscope.console.aliyun.com/)

### Setup

```bash
# 1. Clone the repository
git clone https://github.com/qinrongxin/LangChain4j.git
cd LangChain4j

# 2. Set your API key
# Linux/macOS
export DASHSCOPE_API_KEY=your_api_key_here

# Windows (PowerShell)
$env:DASHSCOPE_API_KEY="your_api_key_here"

# Windows (CMD)
set DASHSCOPE_API_KEY=your_api_key_here
```

### Run Demos

```bash
# Compile the project
mvn clean compile

# Run tests
mvn test

# Run learning demos
mvn exec:java -Dexec.mainClass="AIServicesDemo"
mvn exec:java -Dexec.mainClass="ChatMemoryDemo"
mvn exec:java -Dexec.mainClass="StreamingChatDemo"
mvn exec:java -Dexec.mainClass="Text2Image"
mvn exec:java -Dexec.mainClass="SentimentClassification"

# Run multi-agent data analysis pipeline
mvn exec:java -Dexec.mainClass="dataAnalysis.DataAnalysisDemo"
```

### Run Desktop App

```bash
mvn clean compile
mvn exec:java
```

> The app automatically initializes the SQLite database at `data/assistant.db` on first launch.

---

## 📦 Dependencies

| Dependency | Version | Purpose |
|---|---|---|
| [LangChain4j](https://github.com/langchain4j/langchain4j) | 1.4.0 | Core AI framework |
| [LangGraph4J](https://github.com/bsorrentino/langgraph4j) | 1.8.17 | State graph workflow engine |
| JavaFX | 21 | Desktop UI framework |
| SQLite JDBC | 3.45.1.0 | Embedded database |
| [Tablesaw](https://github.com/jtablesaw/tablesaw) | 0.44.0 | Data analysis (Pandas-like) |
| [JFreeChart](https://github.com/jfree/jfreechart) | 1.5.4 | Chart generation |
| Apache Commons CSV | 1.11.0 | CSV parsing |
| [Apache POI](https://poi.apache.org/) | 5.2.5 | Excel .xls/.xlsx reading & conversion |
| DashScope SDK | 2.21.3 | Direct DashScope API access |
| CommonMark | 0.22.0 | Markdown parsing (streaming renderer) |
| Flexmark | 0.64.0 | Markdown processing (reports) |
| Jackson | 2.17.0 | JSON serialization |
| Lombok | 1.18.38 | Boilerplate reduction |
| JUnit 5 | 5.11.4 | Unit testing |

---

## 🎯 Core Patterns

### AI Services — Annotated Interfaces

The simplest way to create AI-powered services in LangChain4j:

```java
public interface AIAssistant {
    @SystemMessage("You are a helpful assistant.")
    String chat(@UserMessage String message);
}

AIAssistant assistant = AiServices.create(AIAssistant.class, model);
String response = assistant.chat("Hello!");
```

### Tool Calling

Annotate methods with `@Tool` to make them available to the AI:

```java
public class Calculator {
    @Tool("Calculates the square root of a number")
    double squareRoot(@P("The number") double x) {
        return Math.sqrt(x);
    }
}
```

### Multi-Agent Pipeline

Define stateful workflows with LangGraph4J:

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
    // ... conditional routing based on AI plan
    .addEdge("report", END);
```

---

## 📖 Documentation

Comprehensive documentation is available in the **[Wiki](docs/wiki/README.md)**:

| Document | Description |
|----------|-------------|
| [Architecture](docs/wiki/architecture.md) | System architecture, technology stack, design patterns |
| [Data Analysis Pipeline](docs/wiki/data-analysis-pipeline.md) | Multi-agent CSV analysis with LangGraph4J |
| [Desktop Application](docs/wiki/desktop-app.md) | JavaFX chat app documentation |
| [API Reference](docs/wiki/api-reference.md) | Core APIs and extension points |
| [Development Guide](docs/wiki/development-guide.md) | Setup, build, run, contribution |

Also see [CLAUDE.md](CLAUDE.md) for AI assistant development guidance.

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

<div align="center">

**Built with ❤️ using [LangChain4j](https://github.com/langchain4j/langchain4j) & [LangGraph4J](https://github.com/bsorrentino/langgraph4j)**

</div>
