package desktop.app;

import desktop.service.SessionService;
import desktop.view.ChatViewController;
import desktop.view.SidebarController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * AI 助手桌面端 — JavaFX Application 入口。
 */
public class DesktopApp extends Application {

    private SidebarController sidebarController;
    private ChatViewController chatController;

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/desktop/fxml/main-view.fxml"));

            BorderPane root = loader.load();

            sidebarController = createSidebarController(loader);
            chatController = createChatController(loader);

            wireControllers();

            Scene scene = new Scene(root, 1024, 700);
            scene.getStylesheets().add(
                getClass().getResource("/desktop/css/theme.css").toExternalForm());

            setupGlobalShortcuts(scene);

            primaryStage.setTitle("AI 助手");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.show();

            initializeApp();

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("启动失败", "应用启动时发生错误: " + e.getMessage());
        }
    }

    private SidebarController createSidebarController(FXMLLoader mainLoader) {
        SidebarController controller = new SidebarController();

        controller.searchField = (javafx.scene.control.TextField)
            mainLoader.getNamespace().get("searchField");
        controller.sessionList = (javafx.scene.control.ListView<desktop.model.ChatSession>)
            mainLoader.getNamespace().get("sessionList");
        controller.btnNewChat = (javafx.scene.control.Button)
            mainLoader.getNamespace().get("btnNewChat");
        controller.btnSettings = (javafx.scene.control.Button)
            mainLoader.getNamespace().get("btnSettings");

        controller.initialize(null, null);
        return controller;
    }

    private ChatViewController createChatController(FXMLLoader mainLoader) {
        ChatViewController controller = new ChatViewController();

        controller.chatHeaderTitle = (javafx.scene.control.Label)
            mainLoader.getNamespace().get("chatHeaderTitle");
        controller.messagesScroll = (javafx.scene.control.ScrollPane)
            mainLoader.getNamespace().get("messagesScroll");
        controller.messagesContainer = (javafx.scene.layout.VBox)
            mainLoader.getNamespace().get("messagesContainer");
        controller.modelSelector = (javafx.scene.control.ComboBox<desktop.model.ModelConfig>)
            mainLoader.getNamespace().get("modelSelector");
        controller.chatInput = (javafx.scene.control.TextArea)
            mainLoader.getNamespace().get("chatInput");
        controller.btnSend = (javafx.scene.control.Button)
            mainLoader.getNamespace().get("btnSend");

        controller.initialize(null, null);
        return controller;
    }

    private void wireControllers() {
        sidebarController.setOnSessionSelected(sessionId -> {
            chatController.switchToSession(sessionId);
        });

        sidebarController.setOnNewChat(() -> {
            var session = chatController.createNewSession();
            sidebarController.addSession(session);
        });

        sidebarController.setOnSettingsClicked(() -> {
            showSettingsDialog();
        });
    }

    private void setupGlobalShortcuts(Scene scene) {
        scene.getAccelerators().put(
            javafx.scene.input.KeyCombination.keyCombination("Ctrl+N"),
            () -> {
                var session = chatController.createNewSession();
                sidebarController.addSession(session);
            }
        );

        scene.getAccelerators().put(
            javafx.scene.input.KeyCombination.keyCombination("Ctrl+K"),
            () -> sidebarController.searchField.requestFocus()
        );
    }

    private void initializeApp() {
        sidebarController.loadSessions();

        SessionService sessionService = new SessionService();
        if (sessionService.getAllSessions().isEmpty()) {
            var session = chatController.createNewSession();
            sidebarController.addSession(session);
        }
    }

    private void showSettingsDialog() {
        Alert dialog = new Alert(Alert.AlertType.NONE);
        dialog.setTitle("设置");
        dialog.setHeaderText(null);

        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/desktop/fxml/settings-dialog.fxml"));
            Parent settingsContent = loader.load();
            dialog.getDialogPane().setContent(settingsContent);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
            dialog.showAndWait();
        } catch (IOException e) {
            showErrorAlert("错误", "无法加载设置面板: " + e.getMessage());
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
