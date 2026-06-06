package desktop.view;

import desktop.model.ModelConfig;
import desktop.service.ModelService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    @FXML private ComboBox<ModelConfig> settingsModelSelector;
    @FXML private Button btnCancelSettings;
    @FXML private Button btnSaveSettings;

    private final ModelService modelService = new ModelService();
    private Runnable onSaved = () -> {};

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        settingsModelSelector.setItems(
            FXCollections.observableArrayList(modelService.getAvailableModels()));
        settingsModelSelector.setCellFactory(list -> modelCell());
        settingsModelSelector.setButtonCell(modelCell());
        settingsModelSelector.setValue(modelService.getCurrentModel());

        btnCancelSettings.setOnAction(event -> close());
        btnSaveSettings.setOnAction(event -> {
            ModelConfig selected = settingsModelSelector.getValue();
            if (selected != null) {
                modelService.setCurrentModel(selected.modelName());
                onSaved.run();
            }
            close();
        });
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    private ListCell<ModelConfig> modelCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(ModelConfig item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.displayName());
            }
        };
    }

    private void close() {
        Stage stage = (Stage) btnSaveSettings.getScene().getWindow();
        stage.close();
    }
}
