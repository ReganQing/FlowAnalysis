package desktop.view;

import desktop.model.ChatMessage;
import desktop.model.ChatSession;
import desktop.model.MessageRole;
import desktop.model.ModelConfig;
import desktop.service.AnalysisService;
import desktop.service.ChatService;
import desktop.service.FileUploadService;
import desktop.service.ModelService;
import desktop.service.SessionService;
import desktop.service.StreamingCallback;
import desktop.service.TitleService;
import desktop.view.component.AgentLogCard;
import desktop.view.component.MessageBubble;
import desktop.view.component.PipelineProgressView;
import desktop.view.component.FilePreviewPanel;
import desktop.view.component.ToolCallCard;
import dataAnalysis.ProgressListener;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
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
    @FXML public Button btnAttach;
    @FXML public SplitPane centerSplitPane;

    private final ChatService chatService = new ChatService();
    private final SessionService sessionService = new SessionService();
    private final ModelService modelService = new ModelService();
    private final TitleService titleService = new TitleService();
    private final FileUploadService fileUploadService = new FileUploadService();
    private final AnalysisService analysisService = new AnalysisService();
    private final ExecutorService backgroundExecutor = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable, "desktop-chat-worker");
        thread.setDaemon(true);
        return thread;
    });

    private String currentSessionId;
    private boolean isStreaming = false;
    private Consumer<String> onSessionTitleUpdated = sessionId -> {};

    /** 暂存的待分析文件路径（上传后、发送前） */
    private Path pendingFilePath;

    /** 右侧文件预览面板 */
    private FilePreviewPanel filePreviewPanel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupModelSelector();
        setupInput();
        setupFileUpload();
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

    private void setupFileUpload() {
        // 附件按钮 — 打开文件选择器
        if (btnAttach != null) {
            btnAttach.setOnAction(e -> openFileChooser());
        }

        // 拖拽支持 — 在聊天输入区域
        if (chatInput != null) {
            chatInput.setOnDragOver(e -> {
                if (e.getDragboard().hasFiles()) {
                    e.acceptTransferModes(TransferMode.COPY);
                    e.consume();
                }
            });
            chatInput.setOnDragDropped(e -> {
                var db = e.getDragboard();
                if (db.hasFiles() && !db.getFiles().isEmpty()) {
                    handleFileDrop(db.getFiles().get(0));
                    e.setDropCompleted(true);
                }
                e.consume();
            });
        }
    }

    private void openFileChooser() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("选择数据文件");
        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Excel 文件", "*.csv", "*.xls", "*.xlsx"),
            new FileChooser.ExtensionFilter("CSV 文件", "*.csv"),
            new FileChooser.ExtensionFilter("所有文件", "*.*")
        );
        File file = chooser.showOpenDialog(btnAttach.getScene().getWindow());
        if (file != null) {
            handleFileDrop(file);
        }
    }

    private void handleFileDrop(File file) {
        Path path = file.toPath();
        if (!fileUploadService.isSupported(path)) {
            showInlineError("不支持的文件格式。仅支持 .csv .xls .xlsx 文件。");
            return;
        }
        if (file.length() > 50 * 1024 * 1024) {
            showInlineError("文件过大，最大支持 50MB。");
            return;
        }

        // 复制到工作目录
        try {
            pendingFilePath = fileUploadService.uploadFile(path);
            chatInput.setText("📎 " + file.getName() + " — 点击发送开始分析");
            chatInput.setEditable(false);
        } catch (Exception e) {
            showInlineError("文件处理失败: " + e.getMessage());
        }
    }

    private void showInlineError(String message) {
        Label error = new Label("⚠ " + message);
        error.setStyle("-fx-text-fill: #F87171; -fx-font-size: 13px; -fx-padding: 4 0;");
        messagesContainer.getChildren().add(error);
        scrollToBottom();
        // 5 秒后自动消失
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(Duration.seconds(5));
        delay.setOnFinished(e -> messagesContainer.getChildren().remove(error));
        delay.play();
    }

    /**
     * 触发数据分析管线 — 带进度条和实时日志。
     */
    private void triggerDataAnalysis(Path filePath) {
        if (currentSessionId == null) return;

        // 清除空状态占位
        if (messagesContainer.getChildren().size() == 1
            && messagesContainer.getChildren().get(0) instanceof Label) {
            messagesContainer.getChildren().clear();
        }

        // 显示用户上传消息
        String fileName = filePath.getFileName().toString();
        ChatMessage userMsg = chatService.saveUserMessage(currentSessionId, "📎 上传文件: " + fileName);
        messagesContainer.getChildren().add(new MessageBubble(userMsg));

        // 创建管线进度条
        PipelineProgressView progressView = new PipelineProgressView();
        messagesContainer.getChildren().add(progressView);

        // 日志卡片跟踪表：节点名 → AgentLogCard
        java.util.Map<String, AgentLogCard> logCards = new java.util.LinkedHashMap<>();

        // 节点名 → 阶段索引映射
        java.util.Map<String, Integer> stageMap = java.util.Map.of(
            "parser", 0, "cleaner", 1, "planner", 2,
            "analyzer", 3, "insight", 3, "chart", 4, "report", 5
        );

        scrollToBottom();
        setInputEnabled(false);

        // 创建 ProgressListener
        ProgressListener listener = new ProgressListener() {
            @Override
            public void onNodeStart(String nodeName, int stageIndex) {
                int idx = stageMap.getOrDefault(nodeName, 0);
                progressView.updateStage(idx, PipelineProgressView.StageStatus.ACTIVE);

                // 创建日志卡片
                AgentLogCard card = new AgentLogCard(nodeName, idx + 1);
                logCards.put(nodeName, card);
                messagesContainer.getChildren().add(card);
                scrollToBottom();
            }

            @Override
            public void onNodeProgress(String nodeName, String message) {
                AgentLogCard card = logCards.get(nodeName);
                if (card != null) {
                    // 简单判断日志级别
                    if (message.contains("思考") || message.contains("分析") || message.contains("考虑")) {
                        card.addLog(message, AgentLogCard.LogLevel.THINKING);
                    } else if (message.contains("生成") || message.contains("执行")) {
                        card.addLog(message, AgentLogCard.LogLevel.ACTION);
                    } else {
                        card.addLog(message, AgentLogCard.LogLevel.DEBUG);
                    }
                    scrollToBottom();
                }
            }

            @Override
            public void onNodeComplete(String nodeName, long durationMs) {
                AgentLogCard card = logCards.get(nodeName);
                if (card != null) {
                    card.addLog("完成", AgentLogCard.LogLevel.RESULT);
                    card.setCompleted(durationMs);
                }
            }

            @Override
            public void onNodeError(String nodeName, String error) {
                int idx = stageMap.getOrDefault(nodeName, 0);
                progressView.updateStage(idx, PipelineProgressView.StageStatus.ERROR);
                AgentLogCard card = logCards.get(nodeName);
                if (card != null) {
                    card.setError(error);
                }
            }

            @Override
            public void onPipelineComplete(dataAnalysis.DataAnalysisGraph.AnalysisResult result) {
                // 最终阶段标记完成
                progressView.updateStage(6, PipelineProgressView.StageStatus.COMPLETED);

                // 显示完成消息
                Label doneLabel = new Label("✅ 分析完成！报告已生成。");
                doneLabel.setStyle("-fx-text-fill: #34D399; -fx-font-size: 14px; -fx-font-weight: 600; -fx-padding: 8 0;");
                messagesContainer.getChildren().add(doneLabel);
                scrollToBottom();
                setInputEnabled(true);

                // 保存 AI 消息到历史
                String summary = result.getSummary() != null ? result.getSummary() : "数据分析报告已生成";
                chatService.saveAiMessage(currentSessionId, summary, "data-analysis");

                // 展开右栏文件预览面板
                showResultPreview(result);
            }

            @Override
            public void onPipelineError(String error) {
                Label errorLabel = new Label("❌ 分析失败: " + error);
                errorLabel.setStyle("-fx-text-fill: #F87171; -fx-font-size: 14px; -fx-padding: 8 0;");
                messagesContainer.getChildren().add(errorLabel);
                scrollToBottom();
                setInputEnabled(true);
            }
        };

        // 后台执行
        analysisService.analyzeFile(filePath.toString(), listener);
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
        placeholder.setStyle("-fx-text-fill: #6B7685; -fx-font-size: 16px; -fx-padding: 40;");
        messagesContainer.getChildren().add(placeholder);
    }

    private void sendMessage() {
        // 如果有待分析的文件，触发数据分析而非聊天
        if (pendingFilePath != null) {
            triggerDataAnalysis(pendingFilePath);
            pendingFilePath = null;
            chatInput.clear();
            chatInput.setEditable(true);
            return;
        }

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

    // ── 右栏预览面板控制 ────────────────────────────────────

    /**
     * 展示分析结果到右侧预览面板。
     */
    private void showResultPreview(dataAnalysis.DataAnalysisGraph.AnalysisResult result) {
        // 懒初始化预览面板
        if (filePreviewPanel == null) {
            filePreviewPanel = new FilePreviewPanel();
        } else {
            filePreviewPanel.clear();
        }

        // 添加报告文件
        if (result.getReportPath() != null) {
            filePreviewPanel.addFile(result.getReportPath());
        }

        // 添加图表文件（扫描 output/charts/ 目录）
        java.nio.file.Path chartsDir = java.nio.file.Path.of("output", "charts");
        if (java.nio.file.Files.isDirectory(chartsDir)) {
            try (var stream = java.nio.file.Files.list(chartsDir)) {
                stream.filter(p -> {
                    String name = p.getFileName().toString().toLowerCase();
                    return name.endsWith(".png") || name.endsWith(".jpg");
                }).sorted().forEach(p -> filePreviewPanel.addFile(p.toString()));
            } catch (Exception ignored) {
                // 图表目录扫描失败不阻断流程
            }
        }

        // 添加源数据文件预览（CSV）
        if (result.getCsvPath() != null) {
            filePreviewPanel.addFile(result.getCsvPath());
        }

        // 展开右栏
        showPreviewPanel(filePreviewPanel);
    }

    // ── SplitPane 面板动画 ──────────────────────────────────

    /**
     * 展开右侧预览面板（动画滑入）。
     * @param content 要放入右侧面板的 Node
     */
    public void showPreviewPanel(javafx.scene.Node content) {
        if (centerSplitPane == null) return;
        if (centerSplitPane.getItems().size() < 2) {
            centerSplitPane.getItems().add(content);
        }
        // 默认 65% 聊天 / 35% 预览
        animateDivider(0.65);
    }

    /** 折叠右侧预览面板。 */
    public void hidePreviewPanel() {
        if (centerSplitPane == null) return;
        animateDivider(1.0);
        // 延迟移除面板内容
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(Duration.millis(300));
        delay.setOnFinished(e -> {
            if (centerSplitPane.getItems().size() > 1) {
                centerSplitPane.getItems().remove(1);
            }
        });
        delay.play();
    }

    private void animateDivider(double targetPosition) {
        double startPosition = centerSplitPane.getDividerPositions()[0];
        javafx.animation.Timeline timeline = new javafx.animation.Timeline();
        javafx.animation.KeyValue kv = new javafx.animation.KeyValue(
            centerSplitPane.getDividers().get(0).positionProperty(), targetPosition);
        javafx.animation.KeyFrame kf = new javafx.animation.KeyFrame(Duration.millis(300), kv);
        timeline.getKeyFrames().add(kf);
        timeline.play();
    }
}
