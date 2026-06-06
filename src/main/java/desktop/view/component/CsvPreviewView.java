package desktop.view.component;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV 数据表格预览 — 使用 TableView 渲染前 1000 行数据。
 */
public class CsvPreviewView extends BorderPane {

    private static final int MAX_PREVIEW_ROWS = 1000;

    public CsvPreviewView() {
        getStyleClass().add("preview-content");
    }

    /**
     * 加载 CSV 文件并渲染表格。
     *
     * @param filePath CSV 文件路径
     */
    public void loadCsv(String filePath) {
        try {
            List<String[]> rows = readCsvRows(filePath);
            if (rows.isEmpty()) {
                setCenter(new Label("文件为空"));
                return;
            }

            String[] headers = rows.get(0);
            int totalRows = rows.size() - 1; // 减去表头

            TableView<ObservableList<String>> table = new TableView<>();
            table.setStyle("-fx-background-color: #1E222A; -fx-text-fill: #B0B8C4;");

            // 创建列
            for (int i = 0; i < headers.length; i++) {
                final int colIndex = i;
                String header = headers[i].isEmpty() ? "列" + (i + 1) : headers[i];

                TableColumn<ObservableList<String>, String> column = new TableColumn<>(header);
                column.setPrefWidth(120);
                column.setCellValueFactory(param -> {
                    ObservableList<String> row = param.getValue();
                    return colIndex < row.size()
                        ? new javafx.beans.property.SimpleStringProperty(row.get(colIndex))
                        : new javafx.beans.property.SimpleStringProperty("");
                });
                table.getColumns().add(column);
            }

            // 填充数据（跳过表头行）
            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
            int displayRows = Math.min(totalRows, MAX_PREVIEW_ROWS);
            for (int i = 1; i <= displayRows; i++) {
                data.add(FXCollections.observableArrayList(rows.get(i)));
            }
            table.setItems(data);

            setCenter(table);

            // 底部状态栏
            HBox statusBar = new HBox(8);
            statusBar.setStyle("-fx-padding: 6 12; -fx-background-color: #181B22;");
            statusBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Label countLabel = new Label(
                String.format("显示 %,d / %,d 行 · %d 列",
                    displayRows, totalRows, headers.length));
            countLabel.setStyle("-fx-text-fill: #6B7685; -fx-font-size: 12px;"
                + " -fx-font-family: 'JetBrains Mono', 'Consolas', monospace;");

            statusBar.getChildren().add(countLabel);
            setBottom(statusBar);

        } catch (Exception e) {
            setCenter(new Label("无法读取文件: " + e.getMessage()));
        }
    }

    /** 读取 CSV 文件的所有行（简易逗号分割）。 */
    private static List<String[]> readCsvRows(String filePath) throws Exception {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                rows.add(parseCsvLine(line));
            }
        }
        return rows;
    }

    /** 简易 CSV 行解析（处理引号内的逗号）。 */
    private static String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString().trim());
        return fields.toArray(new String[0]);
    }
}
