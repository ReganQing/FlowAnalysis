# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **Java Maven learning/demo project** for LangChain4j (v1.4.0) that demonstrates various AI capabilities including chat, memory management, tool/function calling, streaming responses, structured output, image generation, and RAG.

**Requirements:**
- Java 17+
- Maven 3.x
- `DASHSCOPE_API_KEY` environment variable (Alibaba DashScope API key)

## Build and Run Commands

```bash
# Compile the project
mvn clean compile

# Run a specific demo class
mvn exec:java -Dexec.mainClass="AIServicesDemo"
mvn exec:java -Dexec.mainClass="ChatMemoryDemo"
mvn exec:java -Dexec.mainClass="StreamingChatDemo"
mvn exec:java -Dexec.mainClass="Text2Image"

# Run with environment variable
DASHSCOPE_API_KEY=your_key mvn exec:java -Dexec.mainClass="AIServicesDemo"
```

## Architecture

### AI Service Pattern (Annotation-Based)

AI services are defined as **interfaces with LangChain4j annotations**:

```java
public interface AIAssistant {
    @SystemMessage("You are a helpful assistant.")
    String chat(@UserMessage String message);

    TokenStream streamingChat(String message);
}
```

Services are instantiated via `AiServices.create()`:

```java
AIAssistant assistant = AiServices.create(AIAssistant.class, model);
```

### Model Factory Pattern

`ChatModelCreator` provides factory methods for creating chat models. All models use Alibaba DashScope's OpenAI-compatible endpoint:

- **Default endpoint:** `https://dashscope.aliyuncs.com/compatible-mode/v1`
- **Default model:** `qwen-max-latest`
- API key is read from `DASHSCOPE_API_KEY` environment variable

### Tool/Function Calling Pattern

Tools are Java methods annotated with `@Tool`:

```java
public class Calculator {
    @Tool("两数之和")
    int sum(@P(value = "第一个数") int a, @P(value = "第二个数") int b) {
        return a + b;
    }
}
```

When creating AI services with tools:
```java
AiServices.builder(AIAssistant.class)
    .chatModel(model)
    .tools(new Calculator())
    .build();
```

## Package Structure

| Package | Purpose |
|---------|---------|
| `(root)` | Main demo classes showing various LangChain4j capabilities |
| `model/` | ChatModel factory (`ChatModelCreator`) and model implementations |
| `Tool/` | Tool implementations for AI function calling |
| `StructuralOutput/` | Structured output demos using JSON schemas |
| `RAG/` | RAG implementations (currently empty, for future use) |

## Key Dependencies

- `langchain4j` (1.4.0) - Core library
- `langchain4j-open-ai` - OpenAI integration (used for DashScope compatibility)
- `langchain4j-ollama` - Ollama local model support
- `langchain4j-easy-rag` (1.4.0-beta10) - RAG functionality
- `dashscope-sdk-java` (2.21.3) - Alibaba DashScope SDK
- Lombok - Code generation
- Log4j2/SLF4J - Logging

## Demo Classes

| Class | Purpose |
|-------|---------|
| `AIAssistant.java` | Interface defining AI service contracts |
| `AIServicesDemo.java` | Main demo of AI services with Result handling |
| `ChatMemoryDemo.java` | Conversation memory demonstrations |
| `StreamingChatDemo.java` | Streaming response handling |
| `Text2Image.java` | Image generation via DashScope |
| `SentimentClassification.java` | Sentiment analysis example |
| `StructuralOutputDemo*.java` | JSON schema-enforced structured output |
| `PersonExtractor.java` | Entity extraction demo |

## Notes

- This is a **learning/demo project**, not a production application
- No unit tests exist in `src/test/java/`
- No Log4j2 configuration files exist (uses defaults)
- The `RAG/` package is intended for RAG implementations but is currently empty
- API configuration is hardcoded in `ChatModelCreator` (environment variables only for API keys)
