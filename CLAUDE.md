# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Java Maven learning/demo project for **LangChain4j (v1.4.0)** demonstrating AI capabilities: chat, memory, tool/function calling, streaming, structured output, image generation, and a multi-agent data analysis pipeline. Uses Alibaba DashScope (OpenAI-compatible endpoint) as the primary LLM provider.

**Requirements:** Java 17+, Maven 3.x, `DASHSCOPE_API_KEY` env var (Alibaba DashScope API key).

## Build and Run

```bash
mvn clean compile                                        # Compile everything
mvn test                                                 # Run JUnit 5 tests
mvn exec:java                                            # Launch desktop app (default mainClass)
mvn exec:java -Dexec.mainClass="dataAnalysis.DataAnalysisDemo"  # Multi-agent pipeline
```

**Important:** `exec-maven-plugin` is configured with `<classpathScope>test</classpathScope>`, so `src/test/java/` classes are on the classpath when running any main class. This is why demo classes in `src/test/java/` (without package declarations) can be run with just the class name.

On Windows, set the env var in the same command:
```bash
DASHSCOPE_API_KEY=your_key mvn exec:java -Dexec.mainClass="dataAnalysis.DataAnalysisDemo"
```

Learning demos are in `src/test/java/` — run via:
```bash
mvn exec:java -Dexec.mainClass="AIServicesDemo"          # AI Services demo
mvn exec:java -Dexec.mainClass="ChatMemoryDemo"           # Chat memory demo
mvn exec:java -Dexec.mainClass="StreamingChatDemo"        # Streaming demo
mvn exec:java -Dexec.mainClass="Text2Image"               # Image generation
mvn exec:java -Dexec.mainClass="SentimentClassification"   # Sentiment analysis
```

### 桌面端应用 (JavaFX)

```bash
mvn clean compile                                        # Compile
mvn exec:java                                            # Launch desktop app (default mainClass)
```

需要 `DASHSCOPE_API_KEY` 环境变量。数据库自动初始化在 `data/assistant.db`，Schema 定义在 `src/main/resources/desktop/db/schema.sql`。

## Project Structure

```
src/main/java/
├── model/                    # Shared infrastructure (ChatModelCreator, model configs)
├── dataAnalysis/              # Multi-agent data analysis pipeline
│   ├── chart/                 # Chart generation (JFreeChart)
│   ├── model/                 # Domain models (AnalysisPlan, Insight, ReportData, etc.)
│   ├── nodes/                 # 7 LangGraph4J pipeline nodes
│   ├── processing/            # Data profiling & token budget management
│   ├── report/               # HTML report generator
│   ├── router/               # Model routing (ModelRouter interface + implementations)
│   └── tools/                # @Tool-annotated methods for each node
└── desktop/                   # JavaFX desktop chat application
    ├── app/                   # Application entry point (DesktopApp)
    ├── model/                 # Immutable domain records
    ├── repository/            # DAO interfaces + SQLite implementations
    ├── service/               # Business logic (Chat, Session, Analysis, FileUpload)
    └── view/                  # FXML controllers + custom UI components

src/main/resources/
└── desktop/
    ├── css/theme.css          # Dark theme stylesheet
    ├── db/schema.sql          # SQLite table definitions
    └── fxml/                  # JavaFX FXML layouts

src/test/java/
├── (no package)               # Root-level learning demos
│   ├── AIServicesDemo, ChatMemoryDemo, StreamingChatDemo, StreamingChatDemo2
│   ├── Text2Image, ReportDemo, SentimentClassification
│   ├── ServiceWithMemoryForEachUserExample
│   └── AIAssistant (interface used by AIServicesDemo)
├── StructuralOutput/           # Structured output demos
├── Tool/                      # Tool calling demos (Calculator, etc.)
└── dataAnalysis/
    └── DataAnalysisDemo.java  # Entry point for data analysis pipeline
```

## Architecture

### Core Pattern: AI Services as Annotated Interfaces

All demos follow the same pattern — define a Java interface, annotate it, and let LangChain4j generate the implementation:

```java
public interface AIAssistant {
    @SystemMessage("You are a helpful assistant.")
    String chat(@UserMessage String message);
}
AIAssistant assistant = AiServices.create(AIAssistant.class, model);
```

**With tools:** Use `AiServices.builder()` instead of `.create()`, pass `.tools(new Calculator())`.
**With memory:** Pass `.chatMemory(chatMemory)` or `.chatMemoryProvider(id -> ...)` for multi-user scenarios.
**Streaming:** Return `TokenStream` from interface methods; use `StreamingChatModel` from `ChatModelCreator.newStreamingModel()`.

### Model Factory — `model/ChatModelCreator`

Central factory for all chat models. Hardcoded endpoint `https://dashscope.aliyuncs.com/compatible-mode/v1`, model `qwen-max-latest`. API key from `DASHSCOPE_API_KEY` env var. Also supports Ollama via `model/OllamaAIChatModel`.

### Tool Calling — `src/test/java/Tool/`

Methods annotated with `@Tool("description")` are auto-discovered by LangChain4j. Parameters use `@P("description")`. The `Calculator` class is the canonical example.

### Structured Output — `src/test/java/StructuralOutput/`

Two approaches: (1) manual `ResponseFormat` with explicit `JsonSchema` builder, or (2) return a POJO from an interface method and LangChain4j infers the schema automatically.

### Multi-Agent Data Analysis — `src/main/java/dataAnalysis/`

The most complex subsystem. A LangGraph4J state graph pipeline with 7 nodes and conditional routing:

```
CSVParser → DataCleaner → AIPlanner → [DataAnalyzer|Insight|Chart] → ReportGenerator
```

- **State engine:** LangGraph4J `StateGraph` with `AgentState` channels (see `DataAnalysisGraph.java` and `AnalysisState.java`).
- **Nodes:** 7 `NodeAction<AnalysisState>` implementations in `dataAnalysis/nodes/`.
- **AI Planner:** LLM-driven analysis planning with JSON parsing and fallback default plan.
- **Smart data processing:** Three-layer strategy (profile → sample → aggregate), `TokenBudgetManager` for large data.
- **Chart tools:** 7 chart types (bar, pie, stats, line, multi-line, scatter, combo, boxplot) with unified `ChartStyle`.
- **HTML reports:** Single-file `.html` with embedded CSS + Base64 charts, elegant warm-gold theme.
- **Each node** is backed by tools classes in `dataAnalysis/tools/` with `@Tool`-annotated methods.
- **Tools use** Tablesaw (Pandas-like Java library), Apache Commons CSV, JFreeChart.
- **Output** goes to `output/reports/` (HTML) and `output/charts/` (PNG).
- **Input** is CSV sales data — `DataAnalysisDemo` auto-generates sample data if none provided.
- **Model routing:** `ModelRouter` interface + `IntelliModelRouter` implementation in `dataAnalysis/router/`.
- **Progress reporting:** `ProgressListener` interface (6 callbacks: `onNodeStart`, `onNodeProgress`, `onNodeComplete`, `onNodeError`, `onPipelineComplete`, `onPipelineError`) allows UI consumers to track pipeline execution. Nodes are wrapped via `ProgressTrackingNodeAction` which times execution and emits events.
- **Desktop integration:** `AnalysisService` runs `DataAnalysisGraph` on a daemon thread, wrapping all `ProgressListener` callbacks in `Platform.runLater()` for safe JavaFX UI updates.

Key design decisions documented in `docs/plans/2026-06-05-data-analysis-upgrade-design.md`.

### Desktop App — `src/main/java/desktop/`

A JavaFX 21 desktop chat application with a layered architecture:

```
desktop/
├── app/DesktopApp.java           # JavaFX Application entry point, wires controllers
├── model/                        # Immutable domain records (ChatMessage, ChatSession, ModelConfig, MessageRole)
├── repository/                   # DAO interfaces (SessionRepository, ChatRepository, SettingsRepository)
│   └── impl/                     # SQLite implementations (SQLiteSessionRepository, etc.)
├── service/                      # Business logic layer
│   ├── ChatService               # Core chat: AI service, streaming, tool execution, file attachment
│   ├── SessionService            # Session CRUD, auto-save, restore last session
│   ├── TitleService              # AI-generated session titles
│   ├── ModelService              # Model configuration management
│   ├── AnalysisService           # Bridge: desktop → DataAnalysisGraph, manages background thread
│   └── FileUploadService         # File validation, Excel→CSV conversion (Apache POI), 50MB limit
└── view/                         # FXML controllers and custom UI components
    ├── ChatViewController        # Main chat UI — messages, input, model selector, file attach
    ├── SidebarController         # Session list, search, new chat, settings button
    ├── SettingsController        # API key management, model configuration dialog
    └── component/                # Reusable UI components
        ├── MessageBubble         # Single chat message bubble
        ├── MarkdownRenderer      # Full Markdown rendering via WebView (tables, code highlight)
        ├── MarkdownTextFlow      # Lightweight TextFlow-based Markdown for streaming (no WebView)
        ├── ToolCallCard          # Interactive tool execution visualization card
        ├── AgentLogCard          # Pipeline node progress display
        ├── PipelineProgressView  # 7-stage pipeline progress bar
        ├── FilePreviewPanel      # Right-side file preview panel (CSV table, HTML, image)
        ├── CsvPreviewView        # Tablesaw-powered CSV data table preview
        ├── DropZoneOverlay       # Drag-and-drop file upload overlay
        └── PreviewTabBar         # Tab bar for switching preview views
```

**Key patterns:**
- **Immutability:** All model classes are Java `record` types.
- **Repository pattern:** DAO interfaces in `repository/` with SQLite implementations in `repository/impl/`. `DatabaseManager` is a singleton managing the SQLite connection and schema initialization.
- **FXML wiring:** `DesktopApp.start()` manually instantiates controllers and injects FXML-named components — no DI framework.
- **Streaming:** `ChatService` uses `StreamingChatModel` + `StreamingCallback` to push tokens to `MarkdownTextFlow.updateMarkdown()`, with an 80ms throttle and `forceRender()` on completion.
- **Two Markdown renderers:** `MarkdownTextFlow` (lightweight, for streaming) vs `MarkdownRenderer` (WebView-based, for final display with full GFM table support).
- **AnalysisService bridge:** Wraps `DataAnalysisGraph.execute()` in a daemon thread, forwarding all `ProgressListener` callbacks to the JavaFX Application Thread via `Platform.runLater()`.
- **File upload pipeline:** `FileUploadService` accepts .csv/.xls/.xlsx (max 50MB), auto-converts Excel to CSV via Apache POI, stores in `output/uploads/`.
- **CSS theme:** Single theme file at `src/main/resources/desktop/css/theme.css` — dark theme with midnight blue (#0A0E27) base + gold (#D4A853) accents.
- **Keyboard shortcuts:** Ctrl+N (new session), Ctrl+K (focus search).

**Known JavaFX pitfall:** `TextFlow` with `Text("\n")` line breaks leaks the newline node's style to the next line's first glyph. The fix (in `MarkdownTextFlow.addInlineChildren`) uses zero-width space `​\n` and explicitly sets the same style on the break node. See memory `[[javafx-textflow-linebreak-style-bug]]`.

## 📖 Documentation

Comprehensive project documentation is available in the **Wiki**:

- **[Wiki Home](docs/wiki/README.md)** — Documentation index and quick links
- **[Architecture](docs/wiki/architecture.md)** — System architecture, technology stack, and design patterns
- **[Data Analysis Pipeline](docs/wiki/data-analysis-pipeline.md)** — Multi-agent CSV analysis with LangGraph4J
- **[Desktop Application](docs/wiki/desktop-app.md)** — JavaFX chat app documentation
- **[API Reference](docs/wiki/api-reference.md)** — Core APIs and extension points
- **[Development Guide](docs/wiki/development-guide.md)** — Setup, build, run, and contribution guidelines

## Key Dependencies

| Dependency | Purpose |
|---|---|
| `langchain4j` 1.4.0 | Core AI framework — annotations, `AiServices`, memory |
| `langchain4j-open-ai` | DashScope via OpenAI-compatible API |
| `langchain4j-ollama` | Local model support |
| `langgraph4j-core` 1.8.17 | State graph workflow engine for data analysis pipeline |
| `langgraph4j-langchain4j` 1.8.17 | LangGraph4J ↔ LangChain4j integration |
| `langchain4j-easy-rag` 1.4.0-beta10 | RAG (package exists but unused) |
| `dashscope-sdk-java` 2.21.3 | Direct DashScope SDK (used by Text2Image demo in src/test) |
| `tablesaw-core` 0.44.0 | Data analysis in dataAnalysis pipeline |
| `commons-csv` 1.11.0 | CSV parsing |
| `jfreechart` 1.5.4 | Chart generation |
| `flexmark-all` 0.64.0 | Markdown processing |
| `commonmark` 0.22.0 | Markdown parsing (used by MarkdownTextFlow for streaming) |
| `poi-ooxml` 5.2.5 | Excel .xls/.xlsx reading and conversion to CSV |
| `javafx-controls/fxml/web` 21 | Desktop UI framework |
| `sqlite-jdbc` 3.45.1.0 | Embedded database for chat sessions/messages |
| `jackson-databind` 2.17.0 | JSON serialization |
| `lombok` 1.18.38 | Boilerplate reduction (annotations only) |
| JUnit 5 5.11.4 | Testing framework |

## Noteworthy Conventions

- **Comments and user-facing strings are in Chinese** (Chinese tool descriptions, system messages, report content).
- **No package declaration** on root-level demo classes in `src/test/java/` — run with just the class name (`-Dexec.mainClass="AIServicesDemo"`).
- **`dataAnalysis/` does use a package** — entry point `DataAnalysisDemo.java` is in `src/test/java/dataAnalysis/`, run with full qualified name (`-Dexec.mainClass="dataAnalysis.DataAnalysisDemo"`).
- **Tablesaw API quirks** — use `.by("column")` for grouping (NOT `.apply()`), use `countMissing()` not `missingCount()`, use `standardDeviation()` not `stdDev()`. See the resolved-issues section in `docs/DATA_ANALYSIS_WORKFLOW.md` if hitting Tablesaw compilation errors.
- **JFreeChart 1.5.4** — `DefaultBoxAndWhiskerCategoryDataset` is in `org.jfree.data.statistics` (NOT `category`), `EmptyBlock` takes `(double, double)`, `setDefaultItemLabelsVisible` (with 's'), `BoxAndWhiskerRenderer.setMeanVisible` (NOT `setMeanValueVisible`).
- **LangGraph4J 1.8.17** — use `Channels.base(() -> null)` for last-value semantics (NOT `Channels.lastValue()`), `Map.ofEntries()` for >10 entries, `AsyncCommandAction.of(AsyncEdgeAction.edge_async(...))` for conditional edges.
