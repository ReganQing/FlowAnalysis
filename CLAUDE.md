# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Java Maven learning/demo project for **LangChain4j (v1.4.0)** demonstrating AI capabilities: chat, memory, tool/function calling, streaming, structured output, image generation, and a multi-agent data analysis pipeline. Uses Alibaba DashScope (OpenAI-compatible endpoint) as the primary LLM provider.

**Requirements:** Java 17+, Maven 3.x, `DASHSCOPE_API_KEY` env var (Alibaba DashScope API key).

## Build and Run

```bash
mvn clean compile                                        # Compile
mvn exec:java -Dexec.mainClass="AIServicesDemo"          # Run a demo
mvn exec:java -Dexec.mainClass="dataAnalysis.DataAnalysisDemo"  # Multi-agent pipeline
```

On Windows, set the env var in the same command:
```bash
DASHSCOPE_API_KEY=your_key mvn exec:java -Dexec.mainClass="AIServicesDemo"
```

No test suite exists (`src/test/java/` is empty).

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

### Tool Calling — `Tool/`

Methods annotated with `@Tool("description")` are auto-discovered by LangChain4j. Parameters use `@P("description")`. The `Calculator` class is the canonical example.

### Structured Output — `StructuralOutput/`

Two approaches: (1) manual `ResponseFormat` with explicit `JsonSchema` builder, or (2) return a POJO from an interface method and LangChain4j infers the schema automatically.

### Multi-Agent Data Analysis — `dataAnalysis/`

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
- **Model routing:** `ModelRouter` interface defined in `dataAnalysis/router/` (implementation deferred to next phase).

Key design decisions documented in `docs/plans/2026-06-05-data-analysis-upgrade-design.md`.

## Key Dependencies

| Dependency | Purpose |
|---|---|
| `langchain4j` 1.4.0 | Core AI framework — annotations, `AiServices`, memory |
| `langchain4j-open-ai` | DashScope via OpenAI-compatible API |
| `langchain4j-ollama` | Local model support |
| `langgraph4j-core` 1.8.17 | State graph workflow engine for data analysis pipeline |
| `langgraph4j-langchain4j` 1.8.17 | LangGraph4J ↔ LangChain4j integration |
| `langchain4j-easy-rag` 1.4.0-beta10 | RAG (package exists but unused) |
| `dashscope-sdk-java` 2.21.3 | Direct DashScope SDK (used by Text2Image) |
| `tablesaw-core` 0.44.0 | Data analysis in dataAnalysis pipeline |
| `commons-csv` 1.11.0 | CSV parsing |
| `jfreechart` 1.5.4 | Chart generation |
| `flexmark-all` 0.64.0 | Markdown processing |

## Noteworthy Conventions

- **Comments and user-facing strings are in Chinese** (Chinese tool descriptions, system messages, report content).
- **No package declaration** on root-level demo classes — run with just the class name (`-Dexec.mainClass="AIServicesDemo"`).
- **`dataAnalysis/` does use a package** — run with full qualified name (`-Dexec.mainClass="dataAnalysis.DataAnalysisDemo"`).
- **Tablesaw API quirks** — use `.by("column")` for grouping (NOT `.apply()`), use `countMissing()` not `missingCount()`, use `standardDeviation()` not `stdDev()`. See the resolved-issues section in `docs/DATA_ANALYSIS_WORKFLOW.md` if hitting Tablesaw compilation errors.
- **JFreeChart 1.5.4** — `DefaultBoxAndWhiskerCategoryDataset` is in `org.jfree.data.statistics` (NOT `category`), `EmptyBlock` takes `(double, double)`, `setDefaultItemLabelsVisible` (with 's'), `BoxAndWhiskerRenderer.setMeanVisible` (NOT `setMeanValueVisible`).
- **LangGraph4J 1.8.17** — use `Channels.base(() -> null)` for last-value semantics (NOT `Channels.lastValue()`), `Map.ofEntries()` for >10 entries, `AsyncCommandAction.of(AsyncEdgeAction.edge_async(...))` for conditional edges.
- **RAG package** (`RAG/`) is scaffolded but empty.
