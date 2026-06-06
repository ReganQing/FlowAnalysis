package desktop.view;

import desktop.model.ChatSession;
import desktop.model.ModelConfig;
import desktop.service.SessionService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * 侧边栏控制器 — 管理会话列表、搜索、新建对话。
 */
public class SidebarController implements Initializable {

    @FXML public TextField searchField;
    @FXML public ListView<ChatSession> sessionList;
    @FXML public Button btnNewChat;
    @FXML public Button btnSettings;

    private final SessionService sessionService = new SessionService();
    private final ObservableList<ChatSession> sessions = FXCollections.observableArrayList();
    private Consumer<String> onSessionSelected;
    private Runnable onNewChat;
    private Runnable onSettingsClicked;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupSessionList();
        setupSearch();
        setupButtons();
        loadSessions();
    }

    private void setupSessionList() {
        sessionList.setItems(sessions);
        sessionList.setCellFactory(list -> new SessionCell());
        sessionList.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null && onSessionSelected != null) {
                    onSessionSelected.accept(newVal.id());
                }
            }
        );
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isBlank()) {
                loadSessions();
            } else {
                String keyword = newVal.toLowerCase();
                List<ChatSession> filtered = sessionService.getAllSessions().stream()
                    .filter(s -> s.title().toLowerCase().contains(keyword))
                    .toList();
                sessions.setAll(filtered);
            }
        });
    }

    private void setupButtons() {
        btnNewChat.setOnAction(e -> {
            if (onNewChat != null) onNewChat.run();
        });
        btnSettings.setOnAction(e -> {
            if (onSettingsClicked != null) onSettingsClicked.run();
        });
    }

    public void loadSessions() {
        List<ChatSession> all = sessionService.getAllSessions();
        Platform.runLater(() -> sessions.setAll(all));
    }

    public void addSession(ChatSession session) {
        Platform.runLater(() -> {
            sessions.add(0, session);
            sessionList.getSelectionModel().select(0);
        });
    }

    public void removeSession(String sessionId) {
        Platform.runLater(() -> {
            sessions.removeIf(s -> s.id().equals(sessionId));
            sessionService.deleteSession(sessionId);
        });
    }

    public void refreshSession(String sessionId) {
        sessionService.getSession(sessionId).ifPresent(updated -> {
            Platform.runLater(() -> {
                for (int i = 0; i < sessions.size(); i++) {
                    if (sessions.get(i).id().equals(sessionId)) {
                        sessions.set(i, updated);
                        break;
                    }
                }
            });
        });
    }

    public void setOnSessionSelected(Consumer<String> callback) { this.onSessionSelected = callback; }
    public void setOnNewChat(Runnable callback) { this.onNewChat = callback; }
    public void setOnSettingsClicked(Runnable callback) { this.onSettingsClicked = callback; }

    private static class SessionCell extends ListCell<ChatSession> {
        @Override
        protected void updateItem(ChatSession session, boolean empty) {
            super.updateItem(session, empty);
            if (empty || session == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            VBox cell = new VBox(2);
            cell.setPadding(new javafx.geometry.Insets(6, 8, 0, 0));

            Label title = new Label(session.title());
            title.getStyleClass().add("session-title");
            title.setWrapText(true);
            title.setMaxWidth(220);

            ModelConfig config = ModelConfig.findByModelName(session.modelName());
            Label meta = new Label(config.displayName() + "  ·  " + session.formattedTime());
            meta.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");

            cell.getChildren().addAll(title, meta);
            setGraphic(cell);
            setText(null);
        }
    }
}
