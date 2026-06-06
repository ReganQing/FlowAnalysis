package desktop.view;

import desktop.model.ChatMessage;
import desktop.model.ChatSession;
import desktop.model.MessageRole;
import desktop.model.ModelConfig;
import desktop.service.ChatService;
import desktop.service.ModelService;
import desktop.service.SessionService;
import desktop.service.StreamingCallback;
import desktop.service.TitleService;
import desktop.view.component.MessageBubble;
import desktop.view.component.ToolCallCard;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * 聊天主区域控制器 — 管理消息展示、输入、流式输出。
 */
public class ChatViewController implements Initializable {

    @FXML public Label chatHeaderTitle;
    @FXML public ScrollPane messagesScroll;
    @FXML public VBox messagesContainer;
    @FXML public ComboBox<ModelConfig> modelSelector;
    @FXML public TextArea chatInput;
    @FXML public Button btnSend;

    private final ChatService chatService = new ChatService();
    private final SessionService sessionService = new SessionService();
    private final ModelService modelService = new ModelService();
    private final TitleService titleService = new TitleService();
    private final ExecutorService backgroundExecutor = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable, "desktop-chat-worker");
        thread.setDaemon(true);
        return thread;
    });

    private String currentSessionId;
    private boolean isStreaming = false;
    private Consumer<String> onSessionTitleUpdated = sessionId -> {};

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupModelSelector();
        setupInput();
    }

    private void setupModelSelector() {
        modelSelector.setItems(FXCollections.observableArrayList(modelService.getAvailableModels()));
        modelSelector.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(ModelConfig item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.displayName());
            }
        });
        modelSelector.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(ModelConfig item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.displayName());
            }
        });
        modelSelector.setValue(modelService.getCurrentModel());

        modelSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                modelService.setCurrentModel(newVal.modelName());
            }
        });
    }

    public void refreshCurrentModel() {
        modelSelector.setValue(modelService.getCurrentModel());
    }

    private void setupInput() {
        btnSend.setOnAction(e -> sendMessage());
        chatInput.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER && !e.isShiftDown()) {
                e.consume();
                sendMessage();
            }
        });
    }

    public void switchToSession(String sessionId) {
        this.currentSessionId = sessionId;
        sessionService.getSession(sessionId).ifPresent(session -> {
            chatHeaderTitle.setText(session.title());
        });
        loadMessages();
    }

    public ChatSession createNewSession() {
        ModelConfig model = modelSelector.getValue();
        ChatSession session = sessionService.createSession(model.modelName());
        currentSessionId = session.id();
        chatHeaderTitle.setText(session.title());

        Platform.runLater(() -> {
            messagesContainer.getChildren().clear();
            showEmptyState();
        });

        return session;
    }

    private void loadMessages() {
        if (currentSessionId == null) return;

        Platform.runLater(() -> {
            messagesContainer.getChildren().clear();
            List<ChatMessage> messages = chatService.loadHistory(currentSessionId);

            if (messages.isEmpty()) {
                showEmptyState();
                return;
            }

            for (ChatMessage msg : messages) {
                if (msg.role() == MessageRole.USER || msg.role() == MessageRole.AI) {
                    messagesContainer.getChildren().add(new MessageBubble(msg));
                } else if (msg.isToolCall()) {
                    messagesContainer.getChildren().add(new ToolCallCard(
                        msg.toolName(), msg.toolInput(),
                        msg.content(), msg.toolDuration() != null ? msg.toolDuration() : 0,
                        true
                    ));
                }
            }
            scrollToBottom();
        });
    }

    private void showEmptyState() {
        Label placeholder = new Label("开始一段新对话 ✦");
        placeholder.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 16px; -fx-padding: 40;");
        messagesContainer.getChildren().add(placeholder);
    }

    private void sendMessage() {
        String text = chatInput.getText().trim();
        if (text.isEmpty() || isStreaming) return;
        if (currentSessionId == null) return;

        if (messagesContainer.getChildren().size() == 1
            && messagesContainer.getChildren().get(0) instanceof Label) {
            messagesContainer.getChildren().clear();
        }

        ChatMessage userMsg = chatService.saveUserMessage(currentSessionId, text);
        messagesContainer.getChildren().add(new MessageBubble(userMsg));
        chatInput.clear();
        setInputEnabled(false);

        String modelName = modelSelector.getValue().modelName();
        String requestSessionId = currentSessionId;
        StringBuilder responseContent = new StringBuilder();
        MessageBubble responseBubble = MessageBubble.createStreamingPlaceholder(modelName);
        messagesContainer.getChildren().add(responseBubble);
        scrollToBottom();
        isStreaming = true;

        backgroundExecutor.execute(() -> chatService.sendMessage(
            requestSessionId, text, modelName, new StreamingCallback() {
            @Override
            public void onToken(String token) {
                Platform.runLater(() -> {
                    responseContent.append(token);
                    responseBubble.updateContent(responseContent.toString());
                    if (requestSessionId.equals(currentSessionId)) {
                        scrollToBottom();
                    }
                });
            }

            @Override
            public void onComplete(String fullResponse) {
                chatService.saveAiMessage(requestSessionId, fullResponse, modelName);
                boolean shouldGenerateTitle = chatService.loadHistory(requestSessionId).size() <= 2;
                Platform.runLater(() -> {
                    responseBubble.completeContent(fullResponse);
                    setInputEnabled(true);
                    isStreaming = false;
                });
                if (shouldGenerateTitle) {
                    backgroundExecutor.execute(() -> {
                        String title = titleService.generateTitle(text, fullResponse);
                        sessionService.renameSession(requestSessionId, title);
                        Platform.runLater(() -> {
                            if (requestSessionId.equals(currentSessionId)) {
                                chatHeaderTitle.setText(title);
                            }
                            onSessionTitleUpdated.accept(requestSessionId);
                        });
                    });
                }
            }

            @Override
            public void onError(Throwable error) {
                Platform.runLater(() -> {
                    responseBubble.updateContent("错误: " + error.getMessage());
                    setInputEnabled(true);
                    isStreaming = false;
                });
            }

            @Override
            public void onToolCallStart(String toolName, String toolInput) {
                Platform.runLater(() -> {
                    ToolCallCard card = new ToolCallCard(toolName, toolInput, "执行中...", 0, true);
                    messagesContainer.getChildren().add(
                        messagesContainer.getChildren().size() - 1, card);
                });
            }

            @Override
            public void onToolCallComplete(String toolName, String result, long durationMs) {
                Platform.runLater(() -> {
                    chatService.saveToolMessage(requestSessionId, toolName, "", result, durationMs, modelName);
                });
            }
        }));

    }

    private void setInputEnabled(boolean enabled) {
        chatInput.setDisable(!enabled);
        btnSend.setDisable(!enabled);
    }

    private void scrollToBottom() {
        messagesScroll.setVvalue(1.0);
    }

    public String getCurrentSessionId() { return currentSessionId; }
    public void setOnSessionTitleUpdated(Consumer<String> callback) {
        this.onSessionTitleUpdated = callback;
    }
}
